package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.local.UserDao
import com.example.data.model.TaskItem
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val userDao: UserDao,
    private val taskDao: TaskDao
) {
    // User Auth operations
    suspend fun registerUser(username: String, password: String, name: String): Result<User> {
        val existing = userDao.getUserByUsername(username)
        if (existing != null) {
            return Result.failure(Exception("El nombre de usuario ya está registrado"))
        }
        val user = User(username = username.trim(), password = password, name = name.trim())
        val id = userDao.insertUser(user)
        return Result.success(user.copy(id = id.toInt()))
    }

    suspend fun authenticate(username: String, password: String): User? {
        return userDao.authenticate(username.trim(), password)
    }

    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getAnyUser(): User? {
        return userDao.getUserByUsername("joha")
    }

    // Task operations
    fun getTasksForUser(userId: Int): Flow<List<TaskItem>> = taskDao.getTasksForUser(userId)

    suspend fun getPendingTasks(userId: Int): List<TaskItem> = taskDao.getPendingTasksSync(userId)

    suspend fun getTaskById(taskId: Int): TaskItem? = taskDao.getTaskById(taskId)

    suspend fun insertTask(task: TaskItem): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskItem) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskItem) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(taskId: Int) = taskDao.deleteTaskById(taskId)

    suspend fun setTaskCompleted(taskId: Int, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.updateCompletionStatus(taskId, isCompleted, completedAt)
    }
}
