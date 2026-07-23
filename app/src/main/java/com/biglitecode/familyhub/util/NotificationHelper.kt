package com.biglitecode.familyhub.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.biglitecode.familyhub.R

object NotificationHelper {
    const val CHANNEL_ID = "familyhub_tasks"
    const val REMINDER_CHANNEL_ID = "familyhub_reminders"
    private const val CHANNEL_NAME = "Task Updates"
    private const val REMINDER_CHANNEL_NAME = "Family Reminders"
    private var notificationId = 1000

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when tasks are assigned or updated"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recurring family reminders"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showTaskAssignedNotification(
        context: Context,
        taskTitle: String,
        assigneeName: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_familyhub_notification)
            .setContentTitle("New task assigned")
            .setContentText("$taskTitle → $assigneeName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS denied — fail silently; caller may show Toast
        }
    }

    fun showReminderNotification(
        context: Context,
        title: String
    ) {
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_familyhub_notification)
            .setContentTitle("Family Reminder")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS denied
        }
    }
}
