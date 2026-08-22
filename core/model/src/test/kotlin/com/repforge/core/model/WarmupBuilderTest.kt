package com.repforge.core.model

import org.junit.Assert.*
import org.junit.Test

class WarmupBuilderTest {
    @Test fun `empty when light`() {
        assertTrue(WarmupBuilder.build(15.0).isEmpty())
        assertTrue(WarmupBuilder.build(20.0).isEmpty())
    }

    @Test fun `40kg has one`() {
        val w = WarmupBuilder.build(40.0)
        assertTrue(w.any { it.weightKg == 20.0 })
    }

    @Test fun `82_5 has ramp`() {
        val w = WarmupBuilder.build(82.5)
        assertTrue(w.size >= 3)
        assertTrue(w.map { it.weightKg }.distinct().size == w.size)
        assertTrue(w.last().weightKg < 82.5)
    }

    @Test fun `minimal fewer sets`() {
        val full = WarmupBuilder.build(100.0, minimal = false)
        val mini = WarmupBuilder.build(100.0, minimal = true)
        assertTrue(mini.size <= full.size)
    }

    @Test fun `100kg heavy includes intermediate`() {
        val w = WarmupBuilder.build(120.0)
        assertTrue(w.size >= 4)
    }

    @Test fun `all warmup flag true`() {
        val w = WarmupBuilder.build(80.0)
        assertTrue(w.all { it.isWarmup })
    }
}
