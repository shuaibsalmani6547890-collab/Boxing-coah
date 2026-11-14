package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.i18n.AppStrings
import com.example.ui.components.LanguageSwitchPill
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.BoxingCoachViewModel
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object CoachRoute
@Serializable object CombosRoute
@Serializable object ExercisesRoute
@Serializable object ScheduleRoute
@Serializable object ProgressRoute
@Serializable object SettingsRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: BoxingCoachViewModel = viewModel()
            val prefs by vm.prefsState.collectAsStateWithLifecycle()
            ShadowBoxingTheme(themeStyle = prefs.themeStyle) {
                ShadowBoxingApp(viewModel = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShadowBoxingApp(viewModel: BoxingCoachViewModel = viewModel()) {
    val arena = LocalArenaColors.current
    val navController = rememberNavController()
    val prefs by viewModel.prefsState.collectAsStateWithLifecycle()
    val toastMsg by viewModel.statusToast.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val s = AppStrings.get(prefs.language)

    LaunchedEffect(toastMsg) {
        if (toastMsg != null) {
            snackbarHostState.showSnackbar(toastMsg!!)
            viewModel.clearToast()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination

    fun navigateSingleTop(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val primaryTabs = listOf(
        Triple(HomeRoute, s.navHome, Icons.Default.Home),
        Triple(CoachRoute, s.navCoach, Icons.Default.Videocam),
        Triple(CombosRoute, s.navCombos, Icons.Default.SportsMma),
        Triple(ExercisesRoute, s.navExercises, Icons.Default.FitnessCenter),
        Triple(ProgressRoute, s.navProgress, Icons.Default.Insights)
    )

    val allTabs = primaryTabs + listOf(
        Triple(ScheduleRoute, s.navSchedule, Icons.Default.CalendarMonth),
        Triple(SettingsRoute, s.navSettings, Icons.Default.Settings)
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = arena.bg,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = arena.surface,
                        titleContentColor = arena.primary
                    ),
                    title = {
                        Text(s.appTitle, style = MaterialTheme.typography.titleLarge)
                    },
                    actions = {
                        LanguageSwitchPill(
                            language = prefs.language,
                            onToggle = { viewModel.setLanguage(it) }
                        )
                        IconButton(
                            onClick = { navigateSingleTop(ScheduleRoute) },
                            modifier = Modifier.testTag("topbar_schedule_button")
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = s.navSchedule, tint = TextPrimary)
                        }
                        IconButton(
                            onClick = { navigateSingleTop(SettingsRoute) },
                            modifier = Modifier.testTag("topbar_settings_button")
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = s.navSettings, tint = TextPrimary)
                        }
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = arena.surface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        primaryTabs.forEach { (route, label, icon) ->
                            val selected = currentDest?.hasRoute(route::class) == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateSingleTop(route) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = arena.primary,
                                    selectedTextColor = arena.primary,
                                    indicatorColor = arena.primary.copy(alpha = 0.18f),
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("bottom_nav_${label.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = arena.surface
                    ) {
                        allTabs.forEach { (route, label, icon) ->
                            val selected = currentDest?.hasRoute(route::class) == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateSingleTop(route) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = arena.primary,
                                    selectedTextColor = arena.primary,
                                    indicatorColor = arena.primary.copy(alpha = 0.18f)
                                )
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<HomeRoute> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToCoach = { navigateSingleTop(CoachRoute) },
                            onNavigateToSchedule = { navigateSingleTop(ScheduleRoute) }
                        )
                    }
                    composable<CoachRoute> {
                        LiveCoachScreen(viewModel = viewModel)
                    }
                    composable<CombosRoute> {
                        CombosTechniquesScreen(
                            viewModel = viewModel,
                            onLaunchComboInCoach = { navigateSingleTop(CoachRoute) }
                        )
                    }
                    composable<ExercisesRoute> {
                        ExercisesScreen(viewModel = viewModel)
                    }
                    composable<ScheduleRoute> {
                        ScheduleScreen(viewModel = viewModel)
                    }
                    composable<ProgressRoute> {
                        ProgressScreen(viewModel = viewModel)
                    }
                    composable<SettingsRoute> {
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
