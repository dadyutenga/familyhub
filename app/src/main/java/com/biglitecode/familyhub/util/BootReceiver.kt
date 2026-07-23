package com.biglitecode.familyhub.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.biglitecode.familyhub.data.model.FamilyRole
import com.biglitecode.familyhub.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Re-registers all active family reminders and app usage collection
 * after a device reboot. WorkManager jobs may be cleared on reboot;
 * this receiver fetches current data and re-schedules them.
 */
class BootReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // Only re-schedule if a user session exists
        if (!SessionManager.isLoggedIn()) return

        scope.launch {
            // Re-schedule reminders
            runCatching {
                val app = context.applicationContext as com.biglitecode.familyhub.FamilyHubApp
                val repository = app.repository
                val reminders = repository.getReminders()
                ReminderScheduler.rescheduleAllReminders(context, reminders)
            }

            // Re-schedule app usage collection for child devices
            runCatching {
                val user = SessionManager.currentUser.value
                if (user?.role == FamilyRole.CHILD
                    && UsageStatsHelper.hasUsageAccessPermission(context)
                ) {
                    UsageStatsHelper.scheduleUsageCollection(context)
                }
            }
        }
    }
}
