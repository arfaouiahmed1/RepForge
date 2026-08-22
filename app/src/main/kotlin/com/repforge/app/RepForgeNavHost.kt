package com.repforge.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.repforge.core.designsystem.icon.RepForgeNavIcon
import com.repforge.core.designsystem.icon.RepForgeNavIconView
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.catalog.DesignSystemCatalog
import com.repforge.feature.achievements.AchievementsRoute
import com.repforge.feature.exercise.ExerciseCatalogRoute
import com.repforge.feature.exercise.ExerciseDetailRoute
import com.repforge.feature.formlab.FormLabRoute
import com.repforge.feature.lab.LabRoute
import com.repforge.feature.paywall.PaywallRoute
import com.repforge.feature.progress.ProgressRoute
import com.repforge.feature.routine.RoutineRoute
import com.repforge.feature.settings.SettingsRoute
import com.repforge.feature.today.TodayRoute
import com.repforge.feature.workout.WorkoutRoute

enum class TopLevelDestination(val route: String, val label: String, val icon: RepForgeNavIcon) {
    TODAY("today", "Today", RepForgeNavIcon.TODAY),
    PLAN("routine", "Plan", RepForgeNavIcon.PLAN),
    PROGRESS("progress", "Progress", RepForgeNavIcon.PROGRESS),
    LAB("lab", "Lab", RepForgeNavIcon.LAB),
    PROFILE("settings", "Profile", RepForgeNavIcon.PROFILE)
}

@Composable
fun RepForgeNavHost() {
    val navController = rememberNavController()
    var selected by remember { mutableStateOf(TopLevelDestination.TODAY) }

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RepForgeShapes.NavPill,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    TopLevelDestination.values().forEach { dest ->
                        NavigationBarItem(
                            selected = selected == dest,
                            onClick = {
                                selected = dest
                                navController.navigate(dest.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                RepForgeNavIconView(
                                    icon = dest.icon,
                                    selected = selected == dest,
                                    color = if (selected == dest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = TopLevelDestination.TODAY.route) {
                composable(TopLevelDestination.TODAY.route) { TodayRoute(onStartWorkout = { navController.navigate("workout") }) }
                composable(TopLevelDestination.PLAN.route) { RoutineRoute() }
                composable(TopLevelDestination.PROGRESS.route) { ProgressRoute() }
                composable(TopLevelDestination.PROFILE.route) {
                    SettingsRoute(
                        onNavigatePaywall = { navController.navigate("paywall") },
                        onNavigateCatalog = { navController.navigate("catalog_design") },
                        onNavigateAchievements = { navController.navigate("achievements") },
                        onNavigateExercise = { id -> navController.navigate("exercise/$id") },
                        onNavigateExercises = { navController.navigate("catalog") }
                    )
                }
                composable("catalog") { ExerciseCatalogRoute(onExerciseClick = { id -> navController.navigate("exercise/$id") }) }
                composable("exercise/{id}") { backStack -> val id = backStack.arguments?.getString("id") ?: "bench_bb"; ExerciseDetailRoute(exerciseId = id) }
                composable("achievements") { AchievementsRoute() }
                composable("workout") { WorkoutRoute(onFinish = { navController.popBackStack() }) }
                composable("lab") { LabRoute(onNavigateFormLab = { navController.navigate("formlab") }, onNavigateModel = { navController.navigate("model") }) }
                composable("formlab") { FormLabRoute() }
                composable("model") { ModelInsightsPlaceholder() }
                composable("paywall") { PaywallRoute(onSubscribe = { navController.popBackStack() }, onRestore = { }) }
                composable("catalog_design") { DesignSystemCatalog() }
            }
        }
    }
}

@Composable
private fun ModelInsightsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Model insights — Brier 0.146 • 79% bench success • calibrated")
    }
}
