package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AdhkarScreen
import com.example.ui.screens.AlarmRingingScreen
import com.example.ui.screens.DuaScreen
import com.example.ui.screens.FajrAlarmScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrayerTimesDetailScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SurahDetailScreen
import com.example.ui.screens.TasbihScreen
import com.example.ui.screens.TrackerScreen
import com.example.ui.theme.NoorTheme
import com.example.ui.viewmodel.PrayerViewModel

sealed class Screen(val route: String, val title: String, val selectedIcon: Any, val unselectedIcon: Any) {
    object Home : Screen("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Prayers : Screen("prayer_times", "Prayers", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    object Quran : Screen("quran", "Quran", Icons.Filled.AutoStories, Icons.Outlined.AutoStories)
    object Adhkar : Screen("adhkar", "Adhkar", Icons.Filled.SelfImprovement, Icons.Outlined.SelfImprovement)
    object Tasbih : Screen("tasbih", "Tasbih", Icons.Filled.TouchApp, Icons.Outlined.TouchApp)
    object Tracker : Screen("tracker", "Tracker", Icons.Filled.Checklist, Icons.Outlined.Checklist)
}

class MainActivity : ComponentActivity() {

    private val viewModel: PrayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val navigateToAlarm = intent?.getBooleanExtra("navigate_to_alarm", false) ?: false
        val navigateToTracker = intent?.getBooleanExtra("navigate_to_tracker", false) ?: false

        val initialRoute = when {
            navigateToAlarm -> "alarm_ringing"
            navigateToTracker -> "tracker"
            else -> "home"
        }

        setContent {
            val settings = viewModel.getSettingsManager()
            val isDark = isSystemInDarkTheme() || settings.isAmoledDarkTheme

            NoorTheme(darkTheme = isDark) {
                MainAppNavHost(
                    viewModel = viewModel,
                    initialRoute = initialRoute
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@Composable
fun MainAppNavHost(
    viewModel: PrayerViewModel,
    initialRoute: String = "home"
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Prayers,
        Screen.Quran,
        Screen.Adhkar,
        Screen.Tasbih,
        Screen.Tracker
    )

    val showBottomBar = currentRoute in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .testTag("main_bottom_navigation")
                ) {
                    bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon as androidx.compose.ui.graphics.vector.ImageVector
                                    else screen.unselectedIcon as androidx.compose.ui.graphics.vector.ImageVector,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium
                                )
                            },
                            selected = isSelected,
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            ),
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = initialRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPrayerTimes = { navController.navigate("prayer_times") },
                    onNavigateToQibla = { navController.navigate("qibla") },
                    onNavigateToFajrAlarm = { navController.navigate("fajr_alarm") },
                    onNavigateToQuran = { navController.navigate("quran") },
                    onNavigateToAdhkar = { navController.navigate("adhkar") },
                    onNavigateToTasbih = { navController.navigate("tasbih") },
                    onNavigateToTracker = { navController.navigate("tracker") },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onTriggerAlarmTest = { navController.navigate("alarm_ringing") }
                )
            }

            composable("prayer_times") {
                PrayerTimesDetailScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("qibla") {
                QiblaScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("fajr_alarm") {
                FajrAlarmScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onTestAlarmRinging = { navController.navigate("alarm_ringing") }
                )
            }

            composable("alarm_ringing") {
                AlarmRingingScreen(
                    viewModel = viewModel,
                    onDismissSuccess = {
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            composable("quran") {
                QuranScreen(
                    viewModel = viewModel,
                    onNavigateToSurah = { surahNumber ->
                        navController.navigate("surah_detail/$surahNumber")
                    },
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "surah_detail/{surahNumber}",
                arguments = listOf(navArgument("surahNumber") { type = NavType.IntType })
            ) { backStackEntry ->
                val surahNumber = backStackEntry.arguments?.getInt("surahNumber") ?: 1
                SurahDetailScreen(
                    surahNumber = surahNumber,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("adhkar") {
                AdhkarScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("duas") {
                DuaScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("tasbih") {
                TasbihScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("tracker") {
                TrackerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
