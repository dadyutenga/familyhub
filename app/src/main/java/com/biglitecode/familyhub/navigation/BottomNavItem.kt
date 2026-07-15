package com.biglitecode.familyhub.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomNavItem("home", "Home", Icons.Filled.Home)
    data object Tasks : BottomNavItem("tasks", "Tasks", Icons.Filled.Task)
    data object Report : BottomNavItem("report", "Report", Icons.Filled.Assessment)
    data object Settings : BottomNavItem("settings", "Settings", Icons.Filled.Settings)

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

    fun taskDetail(taskId: String) = "task_detail/$taskId"
}
