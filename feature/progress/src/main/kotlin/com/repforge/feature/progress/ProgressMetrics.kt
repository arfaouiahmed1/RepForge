package com.repforge.feature.progress

import com.repforge.core.model.DAY_MILLIS
import com.repforge.core.model.estimated1RM
import com.repforge.core.model.setVolumeKg

/**
 * Pure, UI-free seam for Progress metric derivation (todo 13).
 *
 * Everything here is plain Kotlin: no Android, no Compose, no Room types — so the
 * metric-series logic is unit-testable on the JVM without Robolectric. The ViewModel
 * maps `SetLogEntity` rows into [WorkSetPoint] and the UI consumes the derived series.
 */

/** One logged set reduced to what metric derivation needs. */
data class WorkSetPoint(
    val timestamp: Long,
    val weightKg: Double,
    val reps: Int,
    val isWarmup: Boolean = false,
)

/** The four plotted metrics. [label] is the filter caption and UiState key. */
enum class ProgressMetric(val label: String) {
    STRENGTH("Strength"),
    VOLUME("Volume"),
    REPS("Reps"),
    LOAD("Load"),
    ;

    companion object {
        /** Maps a persisted/selected label back to the metric; unknown labels fall back to STRENGTH. */
        fun fromLabel(label: String): ProgressMetric =
            entries.firstOrNull { it.label == label } ?: STRENGTH
    }
}

/** Work sets only — warmups never contribute to metrics, best set, or volume totals. */
fun workSetsOf(sets: List<WorkSetPoint>): List<WorkSetPoint> = sets.filterNot { it.isWarmup }

/**
 * Buckets work sets by UTC calendar day and reduces each day per metric:
 *  - STRENGTH: max estimated 1RM (Epley) across the day's work sets
 *  - VOLUME:   tonnage sum in kg (weight x completed reps)
 *  - REPS:     completed-rep sum
 *  - LOAD:     max load lifted
 * Days are returned in chronological order regardless of input order.
 * Warmups are excluded. Empty input (or all-warmup input) yields an empty series —
 * callers must render a distinct empty state, never fabricated data.
 */
fun dailyMetricSeries(sets: List<WorkSetPoint>, metric: ProgressMetric): List<Double> =
    workSetsOf(sets)
        .groupBy { it.timestamp / DAY_MILLIS }
        .toList()
        .sortedBy { (day, _) -> day }
        .map { (_, day) ->
            when (metric) {
                ProgressMetric.STRENGTH -> day.maxOf { estimated1RM(it.weightKg, it.reps) }
                ProgressMetric.VOLUME -> day.sumOf { setVolumeKg(it.weightKg, it.reps) }
                ProgressMetric.REPS -> day.sumOf { it.reps.toDouble() }
                ProgressMetric.LOAD -> day.maxOf { it.weightKg }
            }
        }

/** Best (highest e1RM) work set, warmups excluded; null when there are no work sets. */
fun bestWorkSet(sets: List<WorkSetPoint>): WorkSetPoint? =
    workSetsOf(sets).maxByOrNull { estimated1RM(it.weightKg, it.reps) }

/**
 * Normalizes a real series into chart y-fractions (0..1) with headroom margins.
 * A single point sits mid-height; a flat series renders as an honest flat line —
 * no synthetic variance is ever invented for empty or constant data.
 */
fun normalizeToChart(values: List<Double>): List<Float> = when {
    values.isEmpty() -> emptyList()
    values.size == 1 -> listOf(0.5f)
    else -> {
        val lo = values.min()
        val hi = values.max()
        val range = hi - lo
        if (range <= 0.0) {
            List(values.size) { 0.5f }
        } else {
            values.map { ((it - lo) / range * 0.6 + 0.3).toFloat() }
        }
    }
}
