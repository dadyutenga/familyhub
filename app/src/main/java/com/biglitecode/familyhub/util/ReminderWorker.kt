package com.biglitecode.familyhub.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.util.Calendar

/**
 * Worker for DAILY reminders — fires every day at the scheduled time.
 * The repeat interval is 1 day; the initial delay is calculated to hit reminder_time.
 */
class DailyReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        NotificationHelper.showReminderNotification(applicationContext, title)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "reminder_title"
    }
}

/**
 * Worker for WEEKLY / SPECIFIC_DAYS reminders — runs daily, checks if today's
 * day-of-week matches the configured days, and fires the notification only on
 * matching days.
 */
class SpecificDaysReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val daysCsv = inputData.getString(KEY_DAYS) ?: return Result.failure()

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val todayAbbr = DAY_ABBREVS[today] ?: return Result.failure()

        val allowedDays = daysCsv.split(",").map { it.trim().uppercase() }
        if (todayAbbr in allowedDays) {
            NotificationHelper.showReminderNotification(applicationContext, title)
        }
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "reminder_title"
        const val KEY_DAYS = "reminder_days"

        // Calendar.SUNDAY = 1 .. Calendar.SATURDAY = 7
        private val DAY_ABBREVS = mapOf(
            Calendar.SUNDAY to "SUN",
            Calendar.MONDAY to "MON",
            Calendar.TUESDAY to "TUE",
            Calendar.WEDNESDAY to "WED",
            Calendar.THURSDAY to "THU",
            Calendar.FRIDAY to "FRI",
            Calendar.SATURDAY to "SAT"
        )
    }
}
