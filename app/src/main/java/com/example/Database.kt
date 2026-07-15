package com.example

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String, // email or username
    val name: String,
    val email: String,
    val passwordHash: String,
    val streak: Int = 0,
    val lastActiveDate: String = "", // YYYY-MM-DD
    val dailyGoal: Int = 3, // Target reviews per day
    val avatarIndex: Int = 0,
    val dailyGoalPoints: Int = 60, // Default study goal points YYYY-MM-DD
    val targetRank: String = "الأول على الدولة"
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val subject: String,
    val created: String, // YYYY-MM-DD
    val next: String,    // YYYY-MM-DD
    val reviews: Int = 0,
    val reviewDays: String = "" // comma-separated YYYY-MM-DD
)

@Entity(tableName = "review_logs")
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val lessonId: String,
    val lessonTitle: String,
    val reviewDate: String, // YYYY-MM-DD
    val status: String // "COMPLETED", "POSTPONED", or "ADDED"
)

@Entity(tableName = "question_logs")
data class QuestionLog(
    @PrimaryKey val id: String,
    val userId: String,
    val subject: String,
    val lessonId: String?,
    val count: Int,
    val correctCount: Int?,
    val difficulty: String, // "easy", "medium", "hard"
    val date: String, // YYYY-MM-DD
    val source: String // "manual", "ai_chat"
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val icon: String,
    val color: String, // Hex string
    val type: String, // "positive", "negative"
    val targetValue: Int?, // للعادات السلبية: الحد الأقصى بالدقائق (مثلا 30 دقيقة)
    val isPredefined: Boolean,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)

@Entity(tableName = "habit_logs")
data class HabitLog(
    @PrimaryKey val id: String,
    val userId: String,
    val habitId: String,
    val date: String, // YYYY-MM-DD
    val value: Int?,
    val isCompleted: Boolean
)

@Entity(tableName = "daily_scores")
data class DailyScore(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val userId: String,
    val studyScore: Int,
    val habitsCompletedCount: Int,
    val level: Int, // 0 to 4
    val goalPercentage: Float,
    val breakdownJson: String // JSON map of breakdown details
)

@Entity(tableName = "ai_insights")
data class AiInsight(
    @PrimaryKey val id: String,
    val userId: String,
    val type: String, // "daily", "weekly", "monthly"
    val date: String, // YYYY-MM-DD
    val content: String,
    val createdAt: String
)

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey val id: String,
    val userId: String,
    val subject: String,
    val durationMinutes: Int,
    val date: String // YYYY-MM-DD
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE userId = :userId ORDER BY next ASC")
    fun getLessonsForUser(userId: String): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :id LIMIT 1")
    suspend fun getLessonById(id: String): Lesson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLesson(lesson: Lesson)

    @Update
    suspend fun updateLesson(lesson: Lesson)

    @Delete
    suspend fun deleteLesson(lesson: Lesson)

    @Query("DELETE FROM lessons WHERE userId = :userId")
    suspend fun deleteLessonsForUser(userId: String)
}

@Dao
interface ReviewLogDao {
    @Query("SELECT * FROM review_logs WHERE userId = :userId ORDER BY reviewDate DESC")
    fun getLogsForUser(userId: String): Flow<List<ReviewLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReviewLog)

    @Query("DELETE FROM review_logs WHERE userId = :userId")
    suspend fun deleteLogsForUser(userId: String)
}

@Dao
interface QuestionLogDao {
    @Query("SELECT * FROM question_logs WHERE userId = :userId ORDER BY date DESC")
    fun getQuestionLogsForUser(userId: String): Flow<List<QuestionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionLog(log: QuestionLog)

    @Delete
    suspend fun deleteQuestionLog(log: QuestionLog)

    @Query("DELETE FROM question_logs WHERE userId = :userId")
    suspend fun deleteQuestionLogsForUser(userId: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE userId = :userId")
    fun getHabitsForUser(userId: String): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id LIMIT 1")
    suspend fun getHabitById(id: String): Habit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE userId = :userId")
    suspend fun deleteHabitsForUser(userId: String)
}

@Dao
interface HabitLogDao {
    @Query("SELECT * FROM habit_logs WHERE userId = :userId AND date = :date")
    fun getHabitLogsForDate(userId: String, date: String): Flow<List<HabitLog>>

    @Query("SELECT * FROM habit_logs WHERE userId = :userId")
    fun getAllHabitLogs(userId: String): Flow<List<HabitLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitLog(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE userId = :userId AND habitId = :habitId AND date = :date")
    suspend fun deleteHabitLog(userId: String, habitId: String, date: String)

    @Query("DELETE FROM habit_logs WHERE userId = :userId")
    suspend fun deleteHabitLogsForUser(userId: String)
}

@Dao
interface DailyScoreDao {
    @Query("SELECT * FROM daily_scores WHERE userId = :userId ORDER BY date ASC")
    fun getDailyScoresForUser(userId: String): Flow<List<DailyScore>>

    @Query("SELECT * FROM daily_scores WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getDailyScoreByDate(userId: String, date: String): DailyScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyScore(score: DailyScore)

    @Query("DELETE FROM daily_scores WHERE userId = :userId")
    suspend fun deleteDailyScoresForUser(userId: String)
}

@Dao
interface AiInsightDao {
    @Query("SELECT * FROM ai_insights WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAiInsightsForUser(userId: String): Flow<List<AiInsight>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAiInsight(insight: AiInsight)

    @Query("DELETE FROM ai_insights WHERE userId = :userId")
    suspend fun deleteAiInsightsForUser(userId: String)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY date DESC")
    fun getStudySessionsForUser(userId: String): Flow<List<StudySession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudySession(session: StudySession)

    @Query("DELETE FROM study_sessions WHERE userId = :userId")
    suspend fun deleteStudySessionsForUser(userId: String)
}

@Database(
    entities = [
        User::class,
        Lesson::class,
        ReviewLog::class,
        QuestionLog::class,
        Habit::class,
        HabitLog::class,
        DailyScore::class,
        AiInsight::class,
        StudySession::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun reviewLogDao(): ReviewLogDao
    abstract fun questionLogDao(): QuestionLogDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun dailyScoreDao(): DailyScoreDao
    abstract fun aiInsightDao(): AiInsightDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mihad_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
