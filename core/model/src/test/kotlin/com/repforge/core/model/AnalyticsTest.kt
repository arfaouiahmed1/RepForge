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
}
