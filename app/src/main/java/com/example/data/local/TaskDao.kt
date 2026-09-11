package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY isCompleted ASC, createdAt DESC")
    fun getTasksForUser(userId: Int): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC")
    fun getPendingTasksForUser(userId: Int): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 0 ORDER BY createdAt DESC")
    suspend fun getPendingTasksSync(userId: Int): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
    suspend fun getAllPendingTasksSync(): List<TaskItem>

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Int)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :taskId")
    suspend fun updateCompletionStatus(taskId: Int, isCompleted: Boolean, completedAt: Long?)
}
