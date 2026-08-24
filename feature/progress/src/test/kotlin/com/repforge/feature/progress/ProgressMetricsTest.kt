package com.repforge.feature.progress

import com.repforge.core.model.DAY_MILLIS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Regression coverage for the Progress metric-series seam (todo 13):
 * all four metrics, empty input, chronological ordering, warmup exclusion.
 */
class ProgressMetricsTest {

    private fun set(
        day: Int,
        hourMillis: Long = 0,
        weightKg: Double,
        reps: Int,
        isWarmup: Boolean = false,
    ) = WorkSetPoint(
        timestamp = day * DAY_MILLIS + hourMillis,
        weightKg = weightKg,
        reps = reps,
        isWarmup = isWarmup,
    )

    // -- Strength: daily max estimated 1RM (Epley w * (1 + r/30)) -----------------

    @Test
    fun `strength series takes daily max e1RM`() {
        val sets = listOf(
            set(day = 0, weightKg = 100.0, reps = 5),   // e1RM = 116.666...
            set(day = 0, weightKg = 80.0, reps = 10),   // e1RM = 106.666...
            set(day = 1, weightKg = 102.5, reps = 5),   // e1RM = 119.583...
        )
        val series = dailyMetricSeries(sets, ProgressMetric.STRENGTH)
        assertEquals(2, series.size)
        assertEquals(100.0 * (1 + 5 / 30.0), series[0], 1e-9)
        assertEquals(102.5 * (1 + 5 / 30.0), series[1], 1e-9)
    }

    // -- Volume: daily tonnage sum ------------------------------------------------

    @Test
    fun `volume series sums daily tonnage`() {
        val sets = listOf(
            set(day = 0, weightKg = 100.0, reps = 5),  // 500 kg
            set(day = 0, weightKg = 80.0, reps = 10),  // 800 kg
            set(day = 1, weightKg = 60.0, reps = 8),   // 480 kg
        )
        val series = dailyMetricSeries(sets, ProgressMetric.VOLUME)
        assertEquals(listOf(1300.0, 480.0), series)
    }

    // -- Reps: daily completed-rep sum ---------------------------------------------

    @Test
    fun `reps series sums daily completed reps`() {
        val sets = listOf(
            set(day = 0, weightKg = 100.0, reps = 5),
            set(day = 0, weightKg = 80.0, reps = 10),
            set(day = 1, weightKg = 60.0, reps = 8),
        )
        val series = dailyMetricSeries(sets, ProgressMetric.REPS)
        assertEquals(listOf(15.0, 8.0), series)
    }

    // -- Load: daily max load ------------------------------------------------------

    @Test
    fun `load series takes daily max load`() {
        val sets = listOf(
            set(day = 0, weightKg = 100.0, reps = 5),
            set(day = 0, weightKg = 120.0, reps = 1),
            set(day = 1, weightKg = 60.0, reps = 8),
        )
        val series = dailyMetricSeries(sets, ProgressMetric.LOAD)
        assertEquals(listOf(120.0, 60.0), series)
    }

    // -- Empty input ----------------------------------------------------------------

    @Test
    fun `empty input yields empty series for every metric`() {
        ProgressMetric.entries.forEach { metric ->
            assertEquals(emptyList<Double>(), dailyMetricSeries(emptyList(), metric))
        }
        assertNull(bestWorkSet(emptyList()))
        assertEquals(emptyList<Float>(), normalizeToChart(emptyList()))
    }

    @Test
    fun `all-warmup input is treated as empty`() {
        val sets = listOf(set(day = 0, weightKg = 40.0, reps = 10, isWarmup = true))
        ProgressMetric.entries.forEach { metric ->
            assertEquals(emptyList<Double>(), dailyMetricSeries(sets, metric))
        }
        assertNull(bestWorkSet(sets))
    }

    // -- Chronological ordering -------------------------------------------------------

    @Test
    fun `series is chronological regardless of input order`() {
        val sets = listOf(
            set(day = 3, weightKg = 90.0, reps = 5),
            set(day = 0, weightKg = 70.0, reps = 5),
            set(day = 2, weightKg = 85.0, reps = 5),
            set(day = 1, weightKg = 75.0, reps = 5),
        )
        val series = dailyMetricSeries(sets, ProgressMetric.LOAD)
        assertEquals(listOf(70.0, 75.0, 85.0, 90.0), series)
    }

    @Test
    fun `same-day sets collapse into one bucket across intra-day timestamps`() {
        val sets = listOf(
            set(day = 0, hourMillis = 0, weightKg = 80.0, reps = 5),
            set(day = 0, hourMillis = DAY_MILLIS / 2, weightKg = 82.5, reps = 5),
            set(day = 0, hourMillis = DAY_MILLIS - 1, weightKg = 81.0, reps = 5),
        )
        val series = dailyMetricSeries(sets, ProgressMetric.LOAD)
        assertEquals(listOf(82.5), series)
    }

    // -- Warmup exclusion ----------------------------------------------------------------

    @Test
    fun `warmups never inflate any metric or best set`() {
        val work = set(day = 0, weightKg = 100.0, reps = 5)
        val warmupHeavy = set(day = 0, weightKg = 500.0, reps = 20, isWarmup = true)
        val warmupLight = set(day = 1, weightKg = 40.0, reps = 10, isWarmup = true)
        val sets = listOf(warmupHeavy, work, warmupLight)

        assertEquals(
            listOf(100.0 * (1 + 5 / 30.0)),
            dailyMetricSeries(sets, ProgressMetric.STRENGTH),
        )
        assertEquals(listOf(500.0), dailyMetricSeries(sets, ProgressMetric.VOLUME))
        assertEquals(listOf(5.0), dailyMetricSeries(sets, ProgressMetric.REPS))
        assertEquals(listOf(100.0), dailyMetricSeries(sets, ProgressMetric.LOAD))
    }

    @Test
    fun `best work set excludes warmups`() {
        val sets = listOf(
            set(day = 0, weightKg = 60.0, reps = 8, isWarmup = true),
            set(day = 0, weightKg = 100.0, reps = 5),
            set(day = 1, weightKg = 95.0, reps = 3, isWarmup = true),
        )
        val best = bestWorkSet(sets)!!
        assertEquals(100.0, best.weightKg, 1e-9)
        assertEquals(false, best.isWarmup)
    }

    // -- Normalization edges --------------------------------------------------------------

    @Test
    fun `single point normalizes to mid-height without fabricated variance`() {
        assertEquals(listOf(0.5f), normalizeToChart(listOf(1234.5)))
    }

    @Test
    fun `flat series renders as honest flat line`() {
        assertEquals(listOf(0.5f, 0.5f, 0.5f), normalizeToChart(listOf(70.0, 70.0, 70.0)))
    }

    @Test
    fun `metric labels round-trip through fromLabel`() {
        ProgressMetric.entries.forEach { metric ->
            assertEquals(metric, ProgressMetric.fromLabel(metric.label))
        }
        assertEquals(ProgressMetric.STRENGTH, ProgressMetric.fromLabel("nonsense"))
    }
}
