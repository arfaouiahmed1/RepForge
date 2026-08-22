package com.repforge.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import org.junit.Rule
import org.junit.Test

/**
 * Baseline Profile covering critical workout journeys:
 * launch -> Today -> Start Workout -> log set -> rest timer -> open Progress
 * Measure with Macrobenchmark: startup, frame timing, before/after profile.
 */
class BaselineProfileGenerator {
    @get:Rule val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect("com.repforge.app") {
        pressHome()
        startActivityAndWait()
        // TODO: picks up Compose UI via device.findObject / UiDevice
        // 1. Wait for Today
        // 2. Click START
        // 3. Log set (COMPLETE SET)
        // 4. Wait for Rest timer
        // 5. Navigate to Progress
        device.waitForIdle()
    }
}
