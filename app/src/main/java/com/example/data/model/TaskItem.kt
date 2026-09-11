package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val content: String,
    val category: String = "General",
    val priority: String = "Media", // "Alta", "Media", "Baja"
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
