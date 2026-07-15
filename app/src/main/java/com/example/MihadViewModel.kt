@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

class MihadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MihadRepository
    private val context = application.applicationContext

    // Intervals for spaced repetition: Day 1, Day 3, Day 7, Day 14, Day 30, Day 60, Day 120
    private val intervals = listOf(1L, 3L, 7L, 14L, 30L, 60L, 120L)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    val lessons: StateFlow<List<Lesson>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getLessonsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reviewLogs: StateFlow<List<ReviewLog>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getLogsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MihadRepository(db)
        
        // Load last logged-in user if saved in SharedPrefs
        val prefs = application.getSharedPreferences("mihad_prefs", Context.MODE_PRIVATE)
        val lastUserId = prefs.getString("last_user_id", null)
        if (lastUserId != null) {
            viewModelScope.launch {
                val user = repository.getUserById(lastUserId)
                if (user != null) {
                    _currentUser.value = user
                    checkAndUpdateStreak(user)
                }
            }
        }
    }

    fun registerUser(name: String, email: String, password: String, avatarIndex: Int) {
        viewModelScope.launch {
            _authError.value = null
            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                _authError.value = "يرجى ملء جميع الحقول المطلوبة"
                return@launch
            }
            val cleanEmail = email.trim().lowercase()
            val existing = repository.getUserByEmail(cleanEmail)
            if (existing != null) {
                _authError.value = "هذا البريد الإلكتروني مسجل بالفعل"
                return@launch
            }

            // Create new local user
            val userId = UUID.randomUUID().toString()
            val newUser = User(
                id = userId,
                name = name.trim(),
                email = cleanEmail,
                passwordHash = password, // Simple plain storage since it is persistent locally
                streak = 0,
                lastActiveDate = LocalDate.now().toString(),
                dailyGoal = 3,
                avatarIndex = avatarIndex
            )
            repository.insertUser(newUser)
            _currentUser.value = newUser
            saveLastUser(userId)
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            val cleanEmail = email.trim().lowercase()
            val user = repository.getUserByEmail(cleanEmail)
            if (user == null || user.passwordHash != password) {
                _authError.value = "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                return@launch
            }
            _currentUser.value = user
            checkAndUpdateStreak(user)
            saveLastUser(user.id)
        }
    }

    fun logout() {
        _currentUser.value = null
        val prefs = getApplication<Application>().getSharedPreferences("mihad_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("last_user_id").apply()
    }

    fun updateDailyGoal(goal: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(dailyGoal = goal)
            repository.insertUser(updated)
            _currentUser.value = updated
        }
    }

    fun updateAvatar(index: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(avatarIndex = index)
            repository.insertUser(updated)
            _currentUser.value = updated
        }
    }

    private fun saveLastUser(userId: String) {
        val prefs = getApplication<Application>().getSharedPreferences("mihad_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("last_user_id", userId).apply()
    }

    private suspend fun checkAndUpdateStreak(user: User) {
        val today = LocalDate.now()
        val lastActive = if (user.lastActiveDate.isNotEmpty()) LocalDate.parse(user.lastActiveDate) else null
        
        if (lastActive == null) {
            val updated = user.copy(streak = 1, lastActiveDate = today.toString())
            repository.insertUser(updated)
            _currentUser.value = updated
        } else {
            val daysBetween = ChronoUnit.DAYS.between(lastActive, today)
            if (daysBetween == 1L) {
                // Streak increment
                val updated = user.copy(streak = user.streak + 1, lastActiveDate = today.toString())
                repository.insertUser(updated)
                _currentUser.value = updated
            } else if (daysBetween > 1L) {
                // Streak reset
                val updated = user.copy(streak = 1, lastActiveDate = today.toString())
                repository.insertUser(updated)
                _currentUser.value = updated
            } else if (daysBetween == 0L && user.streak == 0) {
                val updated = user.copy(streak = 1)
                repository.insertUser(updated)
                _currentUser.value = updated
            }
        }
    }

    // Spaced repetition scheduler
    fun addLesson(title: String, subject: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now()
            val newLesson = Lesson(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = title.trim(),
                subject = subject,
                created = today.toString(),
                next = today.plusDays(1).toString(), // First review is tomorrow
                reviews = 0,
                reviewDays = ""
            )
            repository.insertLesson(newLesson)
            scheduleReminder(newLesson)
            
            // Record initial log
            val log = ReviewLog(
                userId = user.id,
                lessonId = newLesson.id,
                lessonTitle = newLesson.title,
                reviewDate = today.toString(),
                status = "ADDED"
            )
            repository.insertLog(log)
            updateActiveActivity()
        }
    }

    fun reviewLesson(lesson: Lesson) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now()
            val newReviewsCount = lesson.reviews + 1
            
            // Calculate next repetition delay based on review count
            val delayDays = intervals[minOf(newReviewsCount, intervals.lastIndex)]
            val nextDate = today.plusDays(delayDays)

            val updatedDays = if (lesson.reviewDays.isEmpty()) {
                today.toString()
            } else {
                "${lesson.reviewDays},$today"
            }

            val updatedLesson = lesson.copy(
                reviews = newReviewsCount,
                next = nextDate.toString(),
                reviewDays = updatedDays
            )
            
            repository.updateLesson(updatedLesson)
            scheduleReminder(updatedLesson)

            // Record Log
            val log = ReviewLog(
                userId = user.id,
                lessonId = lesson.id,
                lessonTitle = lesson.title,
                reviewDate = today.toString(),
                status = "COMPLETED"
            )
            repository.insertLog(log)
            updateActiveActivity()
        }
    }

    fun postponeLesson(lesson: Lesson) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now()
            val nextDate = today.plusDays(1) // Postpone to tomorrow

            val updatedLesson = lesson.copy(
                next = nextDate.toString()
            )
            repository.updateLesson(updatedLesson)
            scheduleReminder(updatedLesson)

            val log = ReviewLog(
                userId = user.id,
                lessonId = lesson.id,
                lessonTitle = lesson.title,
                reviewDate = today.toString(),
                status = "POSTPONED"
            )
            repository.insertLog(log)
        }
    }

    fun deleteLesson(lesson: Lesson) {
        viewModelScope.launch {
            repository.deleteLesson(lesson)
        }
    }

    fun clearUserData() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearUserData(user.id)
            val updated = user.copy(streak = 0)
            repository.insertUser(updated)
            _currentUser.value = updated
        }
    }

    private suspend fun updateActiveActivity() {
        val user = _currentUser.value ?: return
        val today = LocalDate.now()
        val lastActive = if (user.lastActiveDate.isNotEmpty()) LocalDate.parse(user.lastActiveDate) else null
        
        if (lastActive == null || ChronoUnit.DAYS.between(lastActive, today) >= 1L) {
            val newStreak = if (lastActive != null && ChronoUnit.DAYS.between(lastActive, today) == 1L) {
                user.streak + 1
            } else {
                1
            }
            val updated = user.copy(streak = newStreak, lastActiveDate = today.toString())
            repository.insertUser(updated)
            _currentUser.value = updated
        }
    }

    private fun scheduleReminder(lesson: Lesson) {
        val today = LocalDate.now()
        val targetDate = LocalDate.parse(lesson.next)
        val delayDays = ChronoUnit.DAYS.between(today, targetDate).coerceAtLeast(1)

        val workRequest = OneTimeWorkRequestBuilder<ReviewReminderWorker>()
            .setInputData(
                Data.Builder()
                    .putString("title", lesson.title)
                    .build()
            )
            .setInitialDelay(delayDays, TimeUnit.DAYS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "review_${lesson.id}",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    // Direct notification testing to fulfill request "زر تست الاشعارات والخ"
    fun triggerTestNotification(title: String, text: String) {
        val channelId = "review_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channel = NotificationChannel(
            channelId,
            "تذكيرات المراجعة",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "تنبيهات مراجعة الدروس اليومية والجدولة الذكية"
            enableLights(true)
            lightColor = android.graphics.Color.GREEN
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()

        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33
        ) {
            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
        }
    }
}
