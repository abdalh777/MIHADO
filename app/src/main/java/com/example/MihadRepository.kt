package com.example

import kotlinx.coroutines.flow.Flow

class MihadRepository(private val db: AppDatabase) {
    private val userDao = db.userDao()
    private val lessonDao = db.lessonDao()
    private val reviewLogDao = db.reviewLogDao()

    suspend fun getUserById(id: String): User? = userDao.getUserById(id)
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User) = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)

    fun getLessonsForUser(userId: String): Flow<List<Lesson>> = lessonDao.getLessonsForUser(userId)
    suspend fun getLessonById(id: String): Lesson? = lessonDao.getLessonById(id)
    suspend fun insertLesson(lesson: Lesson) = lessonDao.insertLesson(lesson)
    suspend fun updateLesson(lesson: Lesson) = lessonDao.updateLesson(lesson)
    suspend fun deleteLesson(lesson: Lesson) = lessonDao.deleteLesson(lesson)
    suspend fun clearUserData(userId: String) {
        lessonDao.deleteLessonsForUser(userId)
        reviewLogDao.deleteLogsForUser(userId)
    }

    fun getLogsForUser(userId: String): Flow<List<ReviewLog>> = reviewLogDao.getLogsForUser(userId)
    suspend fun insertLog(log: ReviewLog) = reviewLogDao.insertLog(log)
}
