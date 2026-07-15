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
    val avatarIndex: Int = 0
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
    val status: String // "COMPLETED" or "POSTPONED"
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

@Database(entities = [User::class, Lesson::class, ReviewLog::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun lessonDao(): LessonDao
    abstract fun reviewLogDao(): ReviewLogDao

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
