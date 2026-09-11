package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TaskReminderReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_CONTENT = "EXTRA_CONTENT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra(EXTRA_TASK_ID, 0)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Tarea pendiente"
        val content = intent.getStringExtra(EXTRA_CONTENT) ?: ""

        NotificationHelper.showTaskReminderNotification(
            context = context,
            taskId = taskId,
            title = title,
            content = content
        )
    }
}
