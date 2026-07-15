@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.concurrent.TimeUnit

class MihadViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MihadRepository
    private val context = application.applicationContext

    // Standard intervals for spaced repetition
    private val intervals = listOf(1L, 3L, 7L, 14L, 30L, 60L, 120L)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Firebase instances
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    // StateFlows
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

    val questionLogs: StateFlow<List<QuestionLog>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getQuestionLogsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<Habit>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getHabitsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyScores: StateFlow<List<DailyScore>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getDailyScoresForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiInsights: StateFlow<List<AiInsight>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getAiInsightsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySession>> = _currentUser
        .flatMapLatest { user ->
            if (user != null) {
                repository.getStudySessionsForUser(user.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentDateHabitLogs = MutableStateFlow<List<HabitLog>>(emptyList())
    val currentDateHabitLogs: StateFlow<List<HabitLog>> = _currentDateHabitLogs.asStateFlow()

    // Smart message state
    private val _dailySmartMessage = MutableStateFlow<String>("جاري تحميل نصائح مدرّبك الذكي...")
    val dailySmartMessage: StateFlow<String> = _dailySmartMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MihadRepository(db)

        // Try to initialize Firebase
        try {
            firebaseAuth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            Log.d("MihadViewModel", "Firebase components initialized successfully.")
        } catch (e: Exception) {
            Log.w("MihadViewModel", "Firebase is not available. Running in local/offline mode.", e)
        }

        // Load last logged-in user if saved in SharedPrefs
        val prefs = application.getSharedPreferences("mihad_prefs", Context.MODE_PRIVATE)
        val lastUserId = prefs.getString("last_user_id", null)
        if (lastUserId != null) {
            viewModelScope.launch {
                val user = repository.getUserById(lastUserId)
                if (user != null) {
                    _currentUser.value = user
                    checkAndUpdateStreak(user)
                    checkAndSeedHabits(user.id)
                    loadHabitLogsForToday(user.id)
                    loadDailySmartMessage(user)
                }
            }
        }
    }

    private fun loadHabitLogsForToday(userId: String) {
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            repository.getHabitLogsForDate(userId, today).collect { logs ->
                _currentDateHabitLogs.value = logs
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

            val localUserExists = repository.getUserByEmail(cleanEmail)
            if (localUserExists != null) {
                _authError.value = "هذا البريد الإلكتروني مسجل بالفعل"
                return@launch
            }

            // Attempt Firebase Registration
            var userId = UUID.randomUUID().toString()
            val auth = firebaseAuth
            if (auth != null) {
                try {
                    val result = auth.createUserWithEmailAndPassword(cleanEmail, password).awaitTask()
                    if (result.user != null) {
                        userId = result.user!!.uid
                    }
                } catch (e: Exception) {
                    Log.w("MihadViewModel", "Firebase Registration failed. Falling back to local.", e)
                }
            }

            val newUser = User(
                id = userId,
                name = name.trim(),
                email = cleanEmail,
                passwordHash = password, // Encrypted or plain since stored locally, Firebase Auth handles the cloud encryption
                streak = 1,
                lastActiveDate = LocalDate.now().toString(),
                dailyGoal = 3,
                avatarIndex = avatarIndex,
                dailyGoalPoints = 60,
                targetRank = "الأول على الدولة"
            )

            repository.insertUser(newUser)
            _currentUser.value = newUser
            saveLastUser(userId)
            checkAndSeedHabits(userId)
            loadHabitLogsForToday(userId)
            syncUserProfileToFirestore(newUser)
            loadDailySmartMessage(newUser)
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            val cleanEmail = email.trim().lowercase()

            // 1. Check local DB
            var localUser = repository.getUserByEmail(cleanEmail)

            // 2. Attempt Firebase Login if local not found or as verification
            val auth = firebaseAuth
            if (auth != null) {
                try {
                    val result = auth.signInWithEmailAndPassword(cleanEmail, password).awaitTask()
                    if (result.user != null) {
                        val uid = result.user!!.uid
                        if (localUser == null) {
                            // User registered on another device, download and construct user
                            localUser = User(
                                id = uid,
                                name = result.user!!.displayName ?: "مستخدم ميهادو",
                                email = cleanEmail,
                                passwordHash = password,
                                streak = 1,
                                lastActiveDate = LocalDate.now().toString(),
                                dailyGoal = 3,
                                avatarIndex = 0,
                                dailyGoalPoints = 60,
                                targetRank = "الأول على الدولة"
                            )
                            repository.insertUser(localUser)
                        }
                    }
                } catch (e: Exception) {
                    Log.w("MihadViewModel", "Firebase Login failed.", e)
                    if (localUser == null) {
                        _authError.value = "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                        return@launch
                    }
                }
            }

            if (localUser == null || localUser.passwordHash != password) {
                _authError.value = "البريد الإلكتروني أو كلمة المرور غير صحيحة"
                return@launch
            }

            _currentUser.value = localUser
            checkAndUpdateStreak(localUser)
            saveLastUser(localUser.id)
            checkAndSeedHabits(localUser.id)
            loadHabitLogsForToday(localUser.id)
            syncUserProfileToFirestore(localUser)
            loadDailySmartMessage(localUser)
        }
    }

    fun logout() {
        _currentUser.value = null
        val prefs = getApplication<Application>().getSharedPreferences("mihad_prefs", Context.MODE_PRIVATE)
        prefs.edit().remove("last_user_id").apply()
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e("MihadViewModel", "Firebase signOut failed", e)
        }
    }

    fun updateDailyGoal(goal: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(dailyGoal = goal)
            repository.insertUser(updated)
            _currentUser.value = updated
            syncUserProfileToFirestore(updated)
        }
    }

    fun updateDailyGoalPoints(points: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(dailyGoalPoints = points)
            repository.insertUser(updated)
            _currentUser.value = updated
            syncUserProfileToFirestore(updated)
            recalculateDailyScore(user.id, LocalDate.now().toString())
        }
    }

    fun updateTargetRank(rank: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(targetRank = rank)
            repository.insertUser(updated)
            _currentUser.value = updated
            syncUserProfileToFirestore(updated)
        }
    }

    fun updateAvatar(index: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(avatarIndex = index)
            repository.insertUser(updated)
            _currentUser.value = updated
            syncUserProfileToFirestore(updated)
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
            syncUserProfileToFirestore(updated)
        } else {
            val daysBetween = ChronoUnit.DAYS.between(lastActive, today)
            if (daysBetween == 1L) {
                val updated = user.copy(streak = user.streak + 1, lastActiveDate = today.toString())
                repository.insertUser(updated)
                _currentUser.value = updated
                syncUserProfileToFirestore(updated)
            } else if (daysBetween > 1L) {
                val updated = user.copy(streak = 1, lastActiveDate = today.toString())
                repository.insertUser(updated)
                _currentUser.value = updated
                syncUserProfileToFirestore(updated)
            } else if (daysBetween == 0L && user.streak == 0) {
                val updated = user.copy(streak = 1)
                repository.insertUser(updated)
                _currentUser.value = updated
                syncUserProfileToFirestore(updated)
            }
        }
    }

    // Habits seed data initialization
    private suspend fun checkAndSeedHabits(userId: String) {
        val userHabits = repository.getHabitsForUser(userId).first()
        if (userHabits.isEmpty()) {
            val predefined = listOf(
                Habit(UUID.randomUUID().toString(), userId, "🕌 الصلاة في وقتها", "🕌", "#1D6B55", "positive", null, true),
                Habit(UUID.randomUUID().toString(), userId, "📿 أذكار الصباح والمساء", "📿", "#F9C846", "positive", null, true),
                Habit(UUID.randomUUID().toString(), userId, "🏃 الرياضة اليومية", "🏃", "#5FAE86", "positive", null, true),
                Habit(UUID.randomUUID().toString(), userId, "📵 تقليل السكرول والفيديوهات القصيرة", "📵", "#FF6F59", "negative", 30, true)
            )
            for (h in predefined) {
                repository.insertHabit(h)
                syncToFirestore("habits", h.id, h)
            }
        }
    }

    fun addCustomHabit(name: String, icon: String, color: String, type: String, targetValue: Int?) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val habit = Habit(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                name = name,
                icon = icon,
                color = color,
                type = type,
                targetValue = targetValue,
                isPredefined = false
            )
            repository.insertHabit(habit)
            syncToFirestore("habits", habit.id, habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
            // Note: Firebase delete logic can be added if needed
        }
    }

    fun toggleHabit(habitId: String, value: Int? = null) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val existingLogs = repository.getHabitLogsForDate(user.id, today).first()
            val existing = existingLogs.firstOrNull { it.habitId == habitId }

            if (existing != null) {
                repository.deleteHabitLog(user.id, habitId, today)
            } else {
                val log = HabitLog(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    habitId = habitId,
                    date = today,
                    value = value,
                    isCompleted = true
                )
                repository.insertHabitLog(log)
                syncToFirestore("habit_logs", log.id, log)
            }

            // Recalculate streak for this habit
            updateHabitStreak(user.id, habitId)

            // Recalculate daily score
            recalculateDailyScore(user.id, today)
        }
    }

    private suspend fun updateHabitStreak(userId: String, habitId: String) {
        val habit = repository.getHabitById(habitId) ?: return
        val allLogs = repository.getAllHabitLogs(userId).first()
        val habitLogs = allLogs.filter { it.habitId == habitId && it.isCompleted }.map { it.date }.sortedDescending()

        var currentStreak = 0
        var bestStreak = habit.bestStreak
        var dateToCheck = LocalDate.now()

        // Check if completed today
        if (habitLogs.contains(dateToCheck.toString())) {
            currentStreak++
            dateToCheck = dateToCheck.minusDays(1)
            while (habitLogs.contains(dateToCheck.toString())) {
                currentStreak++
                dateToCheck = dateToCheck.minusDays(1)
            }
        } else {
            // Check if completed yesterday
            dateToCheck = dateToCheck.minusDays(1)
            if (habitLogs.contains(dateToCheck.toString())) {
                currentStreak++
                dateToCheck = dateToCheck.minusDays(1)
                while (habitLogs.contains(dateToCheck.toString())) {
                    currentStreak++
                    dateToCheck = dateToCheck.minusDays(1)
                }
            }
        }

        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        val updated = habit.copy(currentStreak = currentStreak, bestStreak = bestStreak)
        repository.updateHabit(updated)
        syncToFirestore("habits", habit.id, updated)
    }

    // Lessons
    fun addLesson(title: String, subject: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val newLesson = Lesson(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                title = title.trim(),
                subject = subject,
                created = today,
                next = LocalDate.now().plusDays(1).toString(), // First review tomorrow
                reviews = 0,
                reviewDays = ""
            )
            repository.insertLesson(newLesson)
            scheduleReminder(newLesson)
            syncToFirestore("lessons", newLesson.id, newLesson)

            val log = ReviewLog(
                userId = user.id,
                lessonId = newLesson.id,
                lessonTitle = newLesson.title,
                reviewDate = today,
                status = "ADDED"
            )
            repository.insertLog(log)
            syncToFirestore("review_logs", UUID.randomUUID().toString(), log)

            updateActiveActivity()
            recalculateDailyScore(user.id, today)
        }
    }

    fun reviewLesson(lesson: Lesson, difficulty: String = "medium") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now()
            val newReviewsCount = lesson.reviews + 1

            // Adaptive interval adjustment based on student input (سهل / متوسط / صعب)
            val baseDelay = intervals[minOf(newReviewsCount, intervals.lastIndex)]
            val delayDays = when (difficulty.lowercase()) {
                "easy" -> (baseDelay * 1.5).toLong().coerceAtLeast(1)
                "hard" -> (baseDelay * 0.5).toLong().coerceAtLeast(1)
                else -> baseDelay
            }
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
            syncToFirestore("lessons", lesson.id, updatedLesson)

            val log = ReviewLog(
                userId = user.id,
                lessonId = lesson.id,
                lessonTitle = lesson.title,
                reviewDate = today.toString(),
                status = "COMPLETED"
            )
            repository.insertLog(log)
            syncToFirestore("review_logs", UUID.randomUUID().toString(), log)

            updateActiveActivity()
            recalculateDailyScore(user.id, today.toString())
        }
    }

    fun postponeLesson(lesson: Lesson) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now()
            val nextDate = today.plusDays(1)

            val updatedLesson = lesson.copy(
                next = nextDate.toString()
            )
            repository.updateLesson(updatedLesson)
            scheduleReminder(updatedLesson)
            syncToFirestore("lessons", lesson.id, updatedLesson)

            val log = ReviewLog(
                userId = user.id,
                lessonId = lesson.id,
                lessonTitle = lesson.title,
                reviewDate = today.toString(),
                status = "POSTPONED"
            )
            repository.insertLog(log)
            syncToFirestore("review_logs", UUID.randomUUID().toString(), log)

            recalculateDailyScore(user.id, today.toString())
        }
    }

    fun deleteLesson(lesson: Lesson) {
        viewModelScope.launch {
            repository.deleteLesson(lesson)
        }
    }

    // Question Logs
    fun addQuestionLog(subject: String, count: Int, difficulty: String, correctCount: Int? = null) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val log = QuestionLog(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                subject = subject,
                lessonId = null,
                count = count,
                correctCount = correctCount,
                difficulty = difficulty,
                date = today,
                source = "manual"
            )
            repository.insertQuestionLog(log)
            syncToFirestore("question_logs", log.id, log)

            updateActiveActivity()
            recalculateDailyScore(user.id, today)
        }
    }

    // Study Sessions
    fun addStudySession(subject: String, durationMinutes: Int) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val session = StudySession(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                subject = subject,
                durationMinutes = durationMinutes,
                date = today
            )
            repository.insertStudySession(session)
            syncToFirestore("study_sessions", session.id, session)

            updateActiveActivity()
            recalculateDailyScore(user.id, today)
        }
    }

    // Recalculate DSS (Daily Study Score)
    fun recalculateDailyScore(userId: String, date: String) {
        viewModelScope.launch {
            try {
                val allLessons = repository.getLessonsForUser(userId).first()
                val newLessonsCount = allLessons.count { it.created == date }

                val allLogs = repository.getLogsForUser(userId).first()
                val reviewsCompletedCount = allLogs.count { it.reviewDate == date && it.status == "COMPLETED" }

                val allQuestionLogs = repository.getQuestionLogsForUser(userId).first()
                val questionsOnDate = allQuestionLogs.filter { it.date == date }
                var questionsScore = 0.0
                var totalQuestionsCount = 0
                for (q in questionsOnDate) {
                    val weight = when (q.difficulty.lowercase()) {
                        "easy" -> 1.0
                        "medium" -> 1.5
                        "hard" -> 2.0
                        else -> 1.5
                    }
                    questionsScore += q.count * weight
                    totalQuestionsCount += q.count
                }

                val allSessions = repository.getStudySessionsForUser(userId).first()
                val minutesOnDate = allSessions.filter { it.date == date }.sumOf { it.durationMinutes }

                val allHabitLogs = repository.getAllHabitLogs(userId).first()
                val completedHabitsCount = allHabitLogs.count { it.date == date && it.isCompleted }
                val habitsScore = minOf(completedHabitsCount * 5, 20)

                // Equation: DSS = (newLessons * 10) + (reviewsCompleted * 8) + (questionsScore) + (minutesOfStudy / 5) + habitsScore
                val studyScore = (newLessonsCount * 10) + (reviewsCompletedCount * 8) + questionsScore.toInt() + (minutesOnDate / 5) + habitsScore

                val user = repository.getUserById(userId)
                val dailyGoalPoints = user?.dailyGoalPoints ?: 60
                val percentage = if (dailyGoalPoints > 0) studyScore.toFloat() / dailyGoalPoints else 0f

                val level = when {
                    studyScore == 0 -> 0
                    percentage < 0.40f -> 1
                    percentage < 0.70f -> 2
                    percentage < 1.00f -> 3
                    else -> 4 // Gold 🏆
                }

                // Create breakdown JSON
                val breakdown = JSONObject()
                breakdown.put("newLessons", newLessonsCount)
                breakdown.put("reviews", reviewsCompletedCount)
                breakdown.put("questions", totalQuestionsCount)
                breakdown.put("minutes", minutesOnDate)

                val habitsArray = JSONArray()
                val habitMap = repository.getHabitsForUser(userId).first().associateBy { it.id }
                allHabitLogs.filter { it.date == date && it.isCompleted }.forEach {
                    val h = habitMap[it.habitId]
                    if (h != null) {
                        habitsArray.put(h.name)
                    }
                }
                breakdown.put("habits", habitsArray)

                val dailyScore = DailyScore(
                    date = date,
                    userId = userId,
                    studyScore = studyScore,
                    habitsCompletedCount = completedHabitsCount,
                    level = level,
                    goalPercentage = percentage,
                    breakdownJson = breakdown.toString()
                )

                repository.insertDailyScore(dailyScore)
                syncToFirestore("daily_scores", date, dailyScore)
            } catch (e: Exception) {
                Log.e("MihadViewModel", "Error in recalculateDailyScore", e)
            }
        }
    }

    // AI smart messages
    fun loadDailySmartMessage(user: User) {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                
                // Fetch stats
                val allLessons = repository.getLessonsForUser(user.id).first()
                val dueLessonsCount = allLessons.count { it.next == today }

                val todayScores = repository.getDailyScoresForUser(user.id).first()
                val todayScoreObj = todayScores.firstOrNull { it.date == today }
                val todayScore = todayScoreObj?.studyScore ?: 0
                val habitsCompleted = todayScoreObj?.habitsCompletedCount ?: 0

                // Weekly percentage
                val last7Days = (0..6).map { LocalDate.now().minusDays(it.toLong()).toString() }
                val weeklyScores = todayScores.filter { last7Days.contains(it.date) }
                val weeklyCompletionPercentage = if (weeklyScores.isNotEmpty()) {
                    (weeklyScores.sumOf { minOf(it.goalPercentage, 1.0f).toDouble() } / 7.0 * 100).toInt()
                } else {
                    0
                }

                val msg = GeminiHelper.generateDailyMessage(
                    name = user.name,
                    streak = user.streak,
                    dailyGoalPoints = user.dailyGoalPoints,
                    todayScore = todayScore,
                    dueLessonsCount = dueLessonsCount,
                    habitsCompleted = habitsCompleted,
                    weeklyCompletionPercentage = weeklyCompletionPercentage,
                    targetRank = user.targetRank
                )
                _dailySmartMessage.value = msg
            } catch (e: Exception) {
                _dailySmartMessage.value = "انطلق اليوم بقوة وحافظ على استمراريتك لتحقيق حلمك بأن تكون الأول على الدولة! 🚀"
            }
        }
    }

    // Smart input chat parsing (e.g., "أقوله وش درست")
    fun submitSmartNote(note: String, onParsedResult: (List<ParsedActivity>) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonString = GeminiHelper.parseSmartLog(note)
                val cleanJson = jsonString.trim()
                    .removePrefix("```json")
                    .removeSuffix("```")
                    .trim()
                
                val array = JSONArray(cleanJson)
                val list = mutableListOf<ParsedActivity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ParsedActivity(
                            type = obj.optString("type", "new_lesson"),
                            subject = obj.optString("subject", "مواد أخرى"),
                            title = obj.optString("title", ""),
                            count = obj.optInt("count", 1),
                            difficulty = obj.optString("difficulty", "medium")
                        )
                    )
                }
                onParsedResult(list)
            } catch (e: Exception) {
                Log.e("MihadViewModel", "Error parsing smart note", e)
                onParsedResult(emptyList())
            }
        }
    }

    fun saveParsedActivities(activities: List<ParsedActivity>) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            for (act in activities) {
                when (act.type) {
                    "new_lesson" -> {
                        addLesson(act.title.ifBlank { "درس جديد" }, act.subject)
                    }
                    "review" -> {
                        // Find matching lesson to review
                        val lessonsList = repository.getLessonsForUser(user.id).first()
                        val match = lessonsList.firstOrNull { it.subject == act.subject && it.title.contains(act.title, ignoreCase = true) }
                        if (match != null) {
                            reviewLesson(match, act.difficulty)
                        } else {
                            // Create lesson as reviewed
                            val lessonId = UUID.randomUUID().toString()
                            val newLesson = Lesson(
                                id = lessonId,
                                userId = user.id,
                                title = act.title.ifBlank { "مراجعة درس" },
                                subject = act.subject,
                                created = today,
                                next = LocalDate.now().plusDays(3).toString(),
                                reviews = 1,
                                reviewDays = today
                            )
                            repository.insertLesson(newLesson)
                            val log = ReviewLog(
                                userId = user.id,
                                lessonId = lessonId,
                                lessonTitle = newLesson.title,
                                reviewDate = today,
                                status = "COMPLETED"
                            )
                            repository.insertLog(log)
                            recalculateDailyScore(user.id, today)
                        }
                    }
                    "questions" -> {
                        addQuestionLog(act.subject, act.count, act.difficulty)
                    }
                }
            }
            loadDailySmartMessage(user)
        }
    }

    // Generate Periodic AI Narrative Report (Section 7.7.3)
    fun generatePeriodicReport(period: String, onResult: (String) -> Unit) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            try {
                val daysToFetch = if (period == "أسبوعي") 7 else 30
                val today = LocalDate.now()
                val targetDates = (0 until daysToFetch).map { today.minusDays(it.toLong()).toString() }

                val allLessons = repository.getLessonsForUser(user.id).first()
                val lessonsCount = allLessons.count { targetDates.contains(it.created) }

                val allLogs = repository.getLogsForUser(user.id).first()
                val reviewsCount = allLogs.count { targetDates.contains(it.reviewDate) && it.status == "COMPLETED" }

                val allQuestionLogs = repository.getQuestionLogsForUser(user.id).first()
                val questionsCount = allQuestionLogs.filter { targetDates.contains(it.date) }.sumOf { it.count }

                val allScores = repository.getDailyScoresForUser(user.id).first()
                val scoreOnDates = allScores.filter { targetDates.contains(it.date) }
                val avgScore = if (scoreOnDates.isNotEmpty()) scoreOnDates.map { it.studyScore }.average().toFloat() else 0f

                val allHabitLogs = repository.getAllHabitLogs(user.id).first()
                val completedHabitsCount = allHabitLogs.count { targetDates.contains(it.date) && it.isCompleted }

                val totalHabitsPossible = repository.getHabitsForUser(user.id).first().size * daysToFetch

                val reportText = GeminiHelper.generateReport(
                    studentName = user.name,
                    period = period,
                    lessonsCount = lessonsCount,
                    reviewsCount = reviewsCount,
                    questionsCount = questionsCount,
                    avgScore = avgScore,
                    habitsCompletedCount = completedHabitsCount,
                    totalHabitsPossible = totalHabitsPossible,
                    targetRank = user.targetRank
                )

                // Save report to database
                val insight = AiInsight(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    type = period.lowercase(),
                    date = today.toString(),
                    content = reportText,
                    createdAt = System.currentTimeMillis().toString()
                )
                repository.insertAiInsight(insight)
                syncToFirestore("ai_insights", insight.id, insight)

                onResult(reportText)
            } catch (e: Exception) {
                onResult("عذراً، حدث خطأ أثناء إعداد تقريرك الدوري: ${e.localizedMessage}")
            }
        }
    }

    fun clearUserData() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            repository.clearUserData(user.id)
            val updated = user.copy(streak = 0)
            repository.insertUser(updated)
            _currentUser.value = updated
            syncUserProfileToFirestore(updated)
            loadHabitLogsForToday(user.id)
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
            syncUserProfileToFirestore(updated)
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
            androidx.work.ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    private fun syncToFirestore(collectionPath: String, documentId: String, data: Any) {
        val firestoreInstance = firestore ?: return
        val currentUser = firebaseAuth?.currentUser ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreInstance.collection("users")
                    .document(currentUser.uid)
                    .collection(collectionPath)
                    .document(documentId)
                    .set(data)
            } catch (e: Exception) {
                Log.e("MihadViewModel", "Firestore sync failed for $collectionPath/$documentId", e)
            }
        }
    }

    private fun syncUserProfileToFirestore(user: User) {
        val firestoreInstance = firestore ?: return
        val currentUser = firebaseAuth?.currentUser ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val profileData = mapOf(
                    "name" to user.name,
                    "email" to user.email,
                    "avatarIndex" to user.avatarIndex,
                    "dailyGoalPoints" to user.dailyGoalPoints,
                    "targetRank" to user.targetRank,
                    "streak" to user.streak,
                    "lastActiveDate" to user.lastActiveDate
                )
                firestoreInstance.collection("users")
                    .document(currentUser.uid)
                    .collection("profile")
                    .document("details")
                    .set(profileData)
            } catch (e: Exception) {
                Log.e("MihadViewModel", "Firestore profile sync failed", e)
            }
        }
    }

    fun triggerTestNotification(title: String, body: String) {
        val channelId = "mihad_reminders"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "تذكيرات ميهادو"
            val descriptionText = "قناة مخصصة لإشعارات وتذكيرات تطبيق ميهادو"
            val importance = android.app.NotificationManager.IMPORTANCE_DEFAULT
            val channel = android.app.NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        with(androidx.core.app.NotificationManagerCompat.from(context)) {
            try {
                if (androidx.core.app.ActivityCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED || android.os.Build.VERSION.SDK_INT < 33
                ) {
                    notify(System.currentTimeMillis().toInt(), builder.build())
                }
            } catch (e: Exception) {
                Log.e("MihadViewModel", "Failed to trigger test notification", e)
            }
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitTask(): T {
        return kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result, null)
                } else {
                    cont.resumeWith(Result.failure(task.exception ?: Exception("Unknown Firebase error")))
                }
            }
        }
    }
}

// Data class for parsed results
data class ParsedActivity(
    val type: String, // "new_lesson" | "review" | "questions"
    val subject: String,
    val title: String,
    val count: Int,
    val difficulty: String // "easy" | "medium" | "hard"
)
