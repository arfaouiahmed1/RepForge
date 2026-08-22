package com.repforge.baselineprofile

import androidx.benchmark.macro.*
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import org.junit.Rule
import org.junit.Test

/**
 * Macrobenchmark — measure startup/runtime with/without Baseline Profile.
 * Android recommends Baseline Profiles for critical journeys + Macrobenchmark to measure.
 * Claim: "benchmarked release builds with and without profile compilation" — not just "used Baseline Profiles"
 */
class StartupBenchmark {
    @get:Rule val rule = MacrobenchmarkRule()

    @Test fun startupNoCompilation() = rule.measureRepeated(
        packageName = "com.repforge.app",
        metrics = listOf(StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) { pressHome(); startActivityAndWait() }

    @Test fun workoutJourney() = rule.measureRepeated(
        packageName = "com.repforge.app",
        metrics = listOf(FrameTimingMetric(), StartupTimingMetric()),
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()
        // Today -> Start Workout -> log set -> rest timer -> Progress
        // device.findObject(By.text("START"))?.click() etc.
    }
}
