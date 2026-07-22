package com.biglitecode.familyhub.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.biglitecode.familyhub.ui.account.AccountScreen
import com.biglitecode.familyhub.ui.complains.ComplainsScreen
import com.biglitecode.familyhub.ui.contact.ContactScreen
import com.biglitecode.familyhub.ui.dashboard.DashboardScreen
import com.biglitecode.familyhub.ui.feedback.FeedbackScreen
import com.biglitecode.familyhub.ui.help.HelpScreen
import com.biglitecode.familyhub.ui.privacy.PrivacyPolicyScreen
import com.biglitecode.familyhub.ui.report.ReportScreen
import com.biglitecode.familyhub.ui.settings.SettingsScreen
import com.biglitecode.familyhub.ui.tasks.TaskDetailScreen
import com.biglitecode.familyhub.ui.tasks.TasksScreen
import com.biglitecode.familyhub.ui.tasks.TasksViewModel
import com.biglitecode.familyhub.ui.theme.CardCream
import com.biglitecode.familyhub.ui.theme.CreamBackground
import com.biglitecode.familyhub.ui.theme.ForestGreen
import com.biglitecode.familyhub.ui.theme.TextBrown
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyHubNavGraph(
    viewModel: TasksViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    val bottomRoutes = BottomNavItem.items.map { it.route }.toSet()
    val showBottomBar = currentDestination?.route in bottomRoutes

    val topTitle = when (currentDestination?.route) {
        Routes.HOME -> "FamilyHub"
        Routes.TASKS -> "Tasks"
        Routes.REPORT -> "Report"
        Routes.SETTINGS -> "Settings"
        Routes.ACCOUNT -> "Account"
        Routes.FEEDBACK -> "Feedback"
        Routes.COMPLAINS -> "Complaints"
        Routes.HELP -> "Help"
        Routes.CONTACT -> "Contact"
        Routes.PRIVACY -> "Privacy Policy"
        else -> if (currentDestination?.route?.startsWith("task_detail") == true) "Task Detail" else "FamilyHub"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CardCream
            ) {
                FamilyHubDrawerContent(
                    user = user,
                    currentRoute = currentDestination?.route,
                    onItemClick = { route ->
                        scope.launch { drawerState.close() }
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = CreamBackground,
            topBar = {
                TopAppBar(
                    title = { Text(topTitle, color = TextBrown) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = ForestGreen)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = CreamBackground,
                        titleContentColor = TextBrown
                    )
                )
            },
            bottomBar = {
                if (showBottomBar) {
                    // Home is the brand circle; other items go in the pill
                    val navItems = BottomNavItem.items.filter { it.route != Routes.HOME }
                    val isHomeSelected = currentDestination?.route == Routes.HOME
                    val selectedIndex = if (isHomeSelected) -1
                        else navItems.indexOfFirst {
                            currentDestination?.hierarchy?.any { d -> d.route == it.route } == true
                        }
                    FamilyHubBottomNav(
                        items = navItems,
                        selectedIndex = selectedIndex,
                        onSelect = { index ->
                            navController.navigate(navItems[index].route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onBrandClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(padding)
            ) {
                composable(Routes.HOME) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onTaskClick = { navController.navigate(Routes.taskDetail(it)) }
                    )
                }
                composable(Routes.TASKS) {
                    TasksScreen(
                        viewModel = viewModel,
                        onTaskClick = { navController.navigate(Routes.taskDetail(it)) }
                    )
                }
                composable(
                    route = Routes.TASK_DETAIL,
                    arguments = listOf(navArgument("taskId") { type = NavType.StringType })
                ) { entry ->
                    val taskId = entry.arguments?.getString("taskId") ?: return@composable
                    TaskDetailScreen(
                        taskId = taskId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Routes.REPORT) {
                    ReportScreen(viewModel = viewModel)
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(viewModel = viewModel)
                }
                composable(Routes.ACCOUNT) {
                    AccountScreen(viewModel = viewModel)
                }
                composable(Routes.FEEDBACK) {
                    FeedbackScreen(viewModel = viewModel)
                }
                composable(Routes.COMPLAINS) {
                    ComplainsScreen(viewModel = viewModel)
                }
                composable(Routes.HELP) {
                    HelpScreen()
                }
                composable(Routes.CONTACT) {
                    ContactScreen(viewModel = viewModel)
                }
                composable(Routes.PRIVACY) {
                    PrivacyPolicyScreen()
                }
            }
        }
    }
}
