package com.repforge.core.model

import kotlin.math.pow

/**
 * Longitudinal analytics — real statistical meaning, not arbitrary rings.
 * All formulas documented and testable.
 */

// Epley: 1RM = w * (1 + r/30) — best for strength analytics simplicity
fun epley1RM(weightKg: Double, reps: Int): Double = weightKg * (1 + reps / 30.0)

// Brzycki: 1RM = w * 36 / (37 - r)
fun brzycki1RM(weightKg: Double, reps: Int): Double = weightKg * 36.0 / (37 - reps).coerceAtLeast(1)

// Pick Epley for primary display, Brzycki as cross-check
fun estimated1RM(weightKg: Double, reps: Int): Double = epley1RM(weightKg, reps)

fun setVolumeKg(weightKg: Double, reps: Int): Double = weightKg * reps

fun sessionVolume(sets: List<SetLog>): Double = sets.sumOf { setVolumeKg(it.weightKg, it.completedReps) }

fun weeklyVolume(sessions: List<List<SetLog>>): Double = sessions.flatten().sumOf { setVolumeKg(it.weightKg, it.completedReps) }

data class StrengthTrend(
    val current1RM: Double,
    val deltaPercent: Double,
    val weeks: Int,
)

fun strengthTrend(history: List<PersonalRecord>): StrengthTrend? {
    if (history.size < 2) return null
    val sorted = history.sortedBy { it.achievedAt }
    val first = sorted.first().estimated1RM
    val last = sorted.last().estimated1RM
    val delta = if (first == 0.0) 0.0 else (last - first) / first * 100
    return StrengthTrend(current1RM = last, deltaPercent = delta, weeks = 12)
}

fun isPR(existing: PersonalRecord?, candidateWeight: Double, candidateReps: Int): Boolean {
    if (existing == null) return true
    val candidate1RM = estimated1RM(candidateWeight, candidateReps)
    return candidate1RM > existing.estimated1RM
}

// Working volume trend — compare last 4 weeks vs prior 4 weeks
fun volumeTrend(weeklyVolumes: List<Double>): Double? {
    if (weeklyVolumes.size < 8) return null
    val recent = weeklyVolumes.takeLast(4).average()
    val prior = weeklyVolumes.takeLast(8).take(4).average()
    return if (prior == 0.0) null else (recent - prior) / prior * 100
}

/** One logged set reduced to the fields the rolling-PR series needs. */
data class SetPoint(val timestamp: Long, val weightKg: Double, val reps: Int)

const val DAY_MILLIS = 24L * 3600L * 1000L

/**
 * Rolling PR series: bucket sets by calendar day (UTC-day index), take the max
 * estimated 1RM per day, ordered chronologically. This is the sparkline series
 * and the source of first->last delta on Progress.
 */
fun rollingDailyMax1RM(sets: List<SetPoint>): List<Double> =
    if (sets.isEmpty()) {
        emptyList()
    } else {
        sets.groupBy { it.timestamp / DAY_MILLIS }
            .toList()
            .sortedBy { (day, _) -> day }
            .map { (_, group) -> group.maxOf { estimated1RM(it.weightKg, it.reps) } }
    }

/** 0.0 when fewer than two points or first == 0; otherwise percent change across the series. */
fun deltaPercent(series: List<Double>): Double {
    if (series.size < 2) return 0.0
    val first = series.first()
    val last = series.last()
    if (first == 0.0) return 0.0
    return (last - first) / first * 100
}
