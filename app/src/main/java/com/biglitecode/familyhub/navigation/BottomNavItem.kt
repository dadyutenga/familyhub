package com.biglitecode.familyhub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Task
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconOutlined: ImageVector
) {
    data object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    data object Tasks : BottomNavItem("tasks", "Tasks", Icons.Filled.Task, Icons.Outlined.Task)
    data object Report : BottomNavItem("report", "Report", Icons.Filled.Assessment, Icons.Outlined.Assessment)
    data object Settings : BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    companion object {
        val items = listOf(Home, Tasks, Report, Settings)
    }
}

object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val TASK_DETAIL = "task_detail/{taskId}"
    const val REPORT = "report"
    const val SETTINGS = "settings"
    const val ACCOUNT = "account"
    const val FEEDBACK = "feedback"
    const val COMPLAINS = "complains"
    const val HELP = "help"
    const val CONTACT = "contact"
    const val PRIVACY = "privacy"
    const val REMINDERS = "reminders"

    fun taskDetail(taskId: String) = "task_detail/$taskId"
}
