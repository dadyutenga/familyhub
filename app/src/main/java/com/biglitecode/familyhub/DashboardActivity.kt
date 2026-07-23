package com.biglitecode.familyhub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.biglitecode.familyhub.data.session.SessionManager
import com.biglitecode.familyhub.navigation.FamilyHubNavGraph
import com.biglitecode.familyhub.ui.appusage.AppUsageViewModel
import com.biglitecode.familyhub.ui.reminders.RemindersViewModel
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CreamBackground
import com.biglitecode.familyhub.ui.theme.FamilyHubTheme
import com.biglitecode.familyhub.util.ReminderScheduler

class DashboardActivity : ComponentActivity() {

    private val viewModel: TasksViewModel by viewModels {
        val app = application as FamilyHubApp
        TasksViewModel.Factory(app.repository, app.taskRepository)
    }

    private val remindersViewModel: RemindersViewModel by viewModels {
        val app = application as FamilyHubApp
        RemindersViewModel.Factory(app.repository, application)
    }

    private val appUsageViewModel: AppUsageViewModel by viewModels {
        val app = application as FamilyHubApp
        AppUsageViewModel.Factory(app.repository)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                this,
                "Notifications disabled. You can enable them in Settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        requestNotificationPermissionIfNeeded()
        rescheduleRemindersOnStart()
        enableEdgeToEdge()
        setContent {
            FamilyHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CreamBackground
                ) {
                    FamilyHubNavGraph(
                        viewModel = viewModel,
                        remindersViewModel = remindersViewModel,
                        appUsageViewModel = appUsageViewModel,
                        onLogout = {
                            SessionManager.logout()
                            startActivity(
                                Intent(this, LoginActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    /**
     * Re-register all active reminders' WorkManager jobs on app start.
     * Also schedules app usage collection for child devices.
     * This ensures reminders survive device reboots or WorkManager job clearing.
     */
    private fun rescheduleRemindersOnStart() {
        val app = application as FamilyHubApp
        lifecycleScope.launch {
            runCatching {
                val reminders = app.repository.getReminders()
                ReminderScheduler.rescheduleAllReminders(this@DashboardActivity, reminders)
            }
            // Schedule app usage collection on child devices that have permission
            runCatching {
                val user = SessionManager.currentUser.value
                if (user?.role == com.biglitecode.familyhub.data.model.FamilyRole.CHILD
                    && com.biglitecode.familyhub.util.UsageStatsHelper.hasUsageAccessPermission(this@DashboardActivity)
                ) {
                    com.biglitecode.familyhub.util.UsageStatsHelper.scheduleUsageCollection(this@DashboardActivity)
                }
            }
        }
    }
}
