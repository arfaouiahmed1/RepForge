package com.repforge.core.model

import org.junit.Assert.*
import org.junit.Test

class AnalyticsTest {
    @Test fun `epley 100x5 is 116_6`() {
        assertEquals(116.66, epley1RM(100.0, 5), 0.01)
    }

    @Test fun `brzycki 100x5`() {
        assertEquals(112.5, brzycki1RM(100.0, 5), 0.01)
    }

    @Test fun `estimated uses epley`() {
        assertEquals(epley1RM(80.0, 8), estimated1RM(80.0, 8), 0.001)
    }

    @Test fun `session volume sum`() {
        val sets = listOf(
            SetLog("a","s","bench_bb","Bench",1,80.0,8,8,null,null,null,null,false,false,0L,null,null),
            SetLog("b","s","bench_bb","Bench",2,80.0,8,8,null,null,null,null,false,false,0L,null,null),
        )
        assertEquals(1280.0, sessionVolume(sets), 0.001)
    }

    @Test fun `isPR when null is true`() {
        assertTrue(isPR(null, 100.0, 5))
    }

    @Test fun `isPR true when higher`() {
        val pr = PersonalRecord("bench_bb","Bench",100.0,5,116.0,0L)
        assertTrue(isPR(pr, 110.0, 5))
        assertFalse(isPR(pr, 90.0, 5))
    }

    @Test fun `strengthTrend needs 2`() {
        assertNull(strengthTrend(emptyList()))
        assertNull(strengthTrend(listOf(PersonalRecord("a","A",100.0,5,116.0,0L))))
        val t = strengthTrend(listOf(
            PersonalRecord("a","A",100.0,5,100.0,0L),
            PersonalRecord("a","A",110.0,5,110.0,1L)
        ))
        assertNotNull(t)
        assertEquals(10.0, t!!.deltaPercent, 0.01)
    }

    @Test fun `volumeTrend needs 8 weeks`() {
        assertNull(volumeTrend(listOf(100.0,100.0)))
        val vols = List(8) { 100.0 + it*10 }
        val vt = volumeTrend(vols)
        assertNotNull(vt)
    }

    @Test fun `rolling daily max takes best set per day`() {
        val day1 = 10L * DAY_MILLIS
        val day2 = 11L * DAY_MILLIS
        val series = rollingDailyMax1RM(
            listOf(
                SetPoint(day1, 80.0, 5),   // e1RM 93.33
                SetPoint(day1, 100.0, 5),  // e1RM 116.66 <- max
                SetPoint(day2, 90.0, 5),   // e1RM 105.0
            )
        )
        assertEquals(2, series.size)
        assertEquals(116.66, series[0], 0.01)
        assertEquals(105.0, series[1], 0.01)
    }

    @Test fun `rolling daily max orders days chronologically regardless of input order`() {
        val day1 = 10L * DAY_MILLIS
        val day2 = 11L * DAY_MILLIS
        val series = rollingDailyMax1RM(
            listOf(
                SetPoint(day2 + 3600_000L, 95.0, 3),
                SetPoint(day1, 70.0, 8),
            )
        )
        assertEquals(2, series.size)
        assertTrue(series[0] < series[1])
    }

    @Test fun `rolling daily max empty input yields empty series`() {
        assertTrue(rollingDailyMax1RM(emptyList()).isEmpty())
    }

    @Test fun `deltaPercent guards degenerate series`() {
        assertEquals(0.0, deltaPercent(emptyList()), 0.0)
        assertEquals(0.0, deltaPercent(listOf(50.0)), 0.0)
        assertEquals(0.0, deltaPercent(listOf(0.0, 999.0)), 0.0)
    }

    @Test fun `deltaPercent computes first to last change`() {
        assertEquals(10.0, deltaPercent(listOf(100.0, 105.0, 110.0)), 0.001)
        assertEquals(-20.0, deltaPercent(listOf(100.0, 80.0)), 0.001)
    }
}
