package com.repforge.core.testing

import androidx.compose.runtime.Composable

/**
 * Screenshot test helper — Paparazzi / Showkase style.
 * Every PR screenshots: Today, Active workout, Rest, Plan, Progress, Paywall × Light/Dark × font scale.
 * Treats visual regression as failure — critical because UI is the point.
 *
 * Usage in :app/src/test:
 *   @Test fun todayLight() = paparazzi.snapshot { RepForgeTheme(darkTheme=false) { TodayScreen(...) } }
 */
object ScreenshotTest {
    val screens = listOf("Today", "Workout", "Rest", "Plan", "Progress", "Paywall")
    val themes = listOf("Light", "Dark")
    val fontScales = listOf(1.0f, 1.3f)
}

// Golden ML inference vectors — catches model/schema breakage immediately
data class GoldenVector(
    val input: Map<String, Float>,
    val expectedProbability: Float,
    val tolerance: Float = 0.02f,
)

val goldenVectors = listOf(
    GoldenVector(mapOf("load_ratio" to 0.76f, "target_reps" to 8f, "previous_rir" to 2f), 0.78f),
    GoldenVector(mapOf("load_ratio" to 0.85f, "target_reps" to 5f, "previous_rir" to 1f), 0.65f),
)
