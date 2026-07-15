package com.example

import kotlinx.coroutines.flow.Flow

class MihadRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val lessonDao = db.lessonDao()
    private val reviewLogDao = db.reviewLogDao()
    private val questionLogDao = db.questionLogDao()
    private val habitDao = db.habitDao()
    private val habitLogDao = db.habitLogDao()
    private val dailyScoreDao = db.dailyScoreDao()
    private val aiInsightDao = db.aiInsightDao()
    private val studySessionDao = db.studySessionDao()

    // Users
    suspend fun getUserById(id: String): User? = userDao.getUserById(id)
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    // Lessons
    fun getLessonsForUser(userId: String): Flow<List<Lesson>> = lessonDao.getLessonsForUser(userId)
    suspend fun getLessonById(id: String): Lesson? = lessonDao.getLessonById(id)
    suspend fun insertLesson(lesson: Lesson) = lessonDao.insertLesson(lesson)
    suspend fun updateLesson(lesson: Lesson) = lessonDao.updateLesson(lesson)
    suspend fun deleteLesson(lesson: Lesson) = lessonDao.deleteLesson(lesson)

    // Review Logs
    fun getLogsForUser(userId: String): Flow<List<ReviewLog>> = reviewLogDao.getLogsForUser(userId)
    suspend fun insertLog(log: ReviewLog) = reviewLogDao.insertLog(log)

    // Question Logs
    fun getQuestionLogsForUser(userId: String): Flow<List<QuestionLog>> = questionLogDao.getQuestionLogsForUser(userId)
    suspend fun insertQuestionLog(log: QuestionLog) = questionLogDao.insertQuestionLog(log)
    suspend fun deleteQuestionLog(log: QuestionLog) = questionLogDao.deleteQuestionLog(log)

    // Habits
    fun getHabitsForUser(userId: String): Flow<List<Habit>> = habitDao.getHabitsForUser(userId)
    suspend fun getHabitById(id: String): Habit? = habitDao.getHabitById(id)
    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    // Habit Logs
    fun getHabitLogsForDate(userId: String, date: String): Flow<List<HabitLog>> = habitLogDao.getHabitLogsForDate(userId, date)
    fun getAllHabitLogs(userId: String): Flow<List<HabitLog>> = habitLogDao.getAllHabitLogs(userId)
    suspend fun insertHabitLog(log: HabitLog) = habitLogDao.insertHabitLog(log)
    suspend fun deleteHabitLog(userId: String, habitId: String, date: String) = habitLogDao.deleteHabitLog(userId, habitId, date)

    // Daily Scores
    fun getDailyScoresForUser(userId: String): Flow<List<DailyScore>> = dailyScoreDao.getDailyScoresForUser(userId)
    suspend fun getDailyScoreByDate(userId: String, date: String): DailyScore? = dailyScoreDao.getDailyScoreByDate(userId, date)
    suspend fun insertDailyScore(score: DailyScore) = dailyScoreDao.insertDailyScore(score)

    // AI Insights
    fun getAiInsightsForUser(userId: String): Flow<List<AiInsight>> = aiInsightDao.getAiInsightsForUser(userId)
    suspend fun insertAiInsight(insight: AiInsight) = aiInsightDao.insertAiInsight(insight)

    // Study Sessions
    fun getStudySessionsForUser(userId: String): Flow<List<StudySession>> = studySessionDao.getStudySessionsForUser(userId)
    suspend fun insertStudySession(session: StudySession) = studySessionDao.insertStudySession(session)

    // Clear User Data
    suspend fun clearUserData(userId: String) {
        lessonDao.deleteLessonsForUser(userId)
        reviewLogDao.deleteLogsForUser(userId)
        questionLogDao.deleteQuestionLogsForUser(userId)
        habitDao.deleteHabitsForUser(userId)
        habitLogDao.deleteHabitLogsForUser(userId)
        dailyScoreDao.deleteDailyScoresForUser(userId)
        aiInsightDao.deleteAiInsightsForUser(userId)
        studySessionDao.deleteStudySessionsForUser(userId)
    }
}
