package com.biglitecode.familyhub.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.biglitecode.familyhub.data.model.FamilyReminder
import com.biglitecode.familyhub.data.model.RepeatType
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Schedules / cancels WorkManager periodic work for family reminders.
 *
 * - DAILY reminders: PeriodicWorkRequest with 1-day repeat interval, initial delay
 *   calculated to hit reminder_time.
 * - WEEKLY / SPECIFIC_DAYS reminders: daily worker that checks day-of-week before
 *   firing.
 */
object ReminderScheduler {

    private const val WORK_TAG_PREFIX = "reminder_"

    /**
     * Schedule a single reminder. Uses [ExistingPeriodicWorkPolicy.UPDATE] so calling
     * this again for the same ID updates the schedule without creating duplicates.
     */
    fun scheduleReminder(context: Context, reminder: FamilyReminder) {
        if (!reminder.isActive) return

        val workTag = WORK_TAG_PREFIX + reminder.id
        val delayMs = calculateInitialDelay(reminder.reminderTime)

        val request = when (reminder.repeatType) {
            RepeatType.DAILY -> {
                val inputData = Data.Builder()
                    .putString(DailyReminderWorker.KEY_TITLE, reminder.title)
                    .build()
                PeriodicWorkRequestBuilder<DailyReminderWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .addTag(workTag)
                    .setInputData(inputData)
                    .build()
            }
            RepeatType.WEEKLY, RepeatType.SPECIFIC_DAYS -> {
                val daysValue = reminder.daysOfWeek ?: "MON,TUE,WED,THU,FRI,SAT,SUN"
                val inputData = Data.Builder()
                    .putString(SpecificDaysReminderWorker.KEY_TITLE, reminder.title)
                    .putString(SpecificDaysReminderWorker.KEY_DAYS, daysValue)
                    .build()
                PeriodicWorkRequestBuilder<SpecificDaysReminderWorker>(1, TimeUnit.DAYS)
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .addTag(workTag)
                    .setInputData(inputData)
                    .build()
            }
        }

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workTag,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /**
     * Cancel a single reminder's scheduled work.
     */
    fun cancelReminder(context: Context, reminderId: String) {
        val workTag = WORK_TAG_PREFIX + reminderId
        WorkManager.getInstance(context).cancelUniqueWork(workTag)
    }

    /**
     * Re-register all active reminders. Called on app start and after device reboot
     * to ensure reminders survive WorkManager's job clearing on reboot.
     */
    fun rescheduleAllReminders(context: Context, reminders: List<FamilyReminder>) {
        reminders.forEach { reminder ->
            if (reminder.isActive) {
                scheduleReminder(context, reminder)
            }
        }
    }

    /**
     * Calculate the initial delay in milliseconds from now until the next occurrence
     * of [timeStr] (format "HH:mm"). If the time has already passed today, schedules
     * for tomorrow.
     */
    private fun calculateInitialDelay(timeStr: String): Long {
        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If target time has already passed today, schedule for tomorrow
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
