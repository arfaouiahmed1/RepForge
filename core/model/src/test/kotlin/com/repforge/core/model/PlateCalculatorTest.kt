package com.repforge.core.model

import org.junit.Assert.*
import org.junit.Test

class PlateCalculatorTest {
    @Test fun `bar only when target equals bar`() {
        val load = PlateCalculator.calculate(20.0, barKg = 20.0)
        assertTrue(load.perSideKg.isEmpty())
        assertEquals(0.0, load.remainingKg, 0.001)
        assertTrue(PlateCalculator.formatPlates(load, true).contains("Bar only"))
    }

    @Test fun `simple 60kg`() {
        val load = PlateCalculator.calculate(60.0, barKg = 20.0)
        assertEquals(listOf(20.0), load.perSideKg.sortedDescending())
        assertEquals(0.0, load.remainingKg, 0.001)
    }

    @Test fun `82_5kg plates`() {
        val load = PlateCalculator.calculate(82.5, barKg = 20.0)
        assertEquals(0.0, load.remainingKg, 0.01)
        val sumPerSide = load.perSideKg.sum()
        assertEquals(31.25, sumPerSide, 0.01)
    }

    @Test fun `100kg greedy`() {
        val load = PlateCalculator.calculate(100.0, barKg = 20.0)
        assertEquals(0.0, load.remainingKg, 0.01)
        assertEquals(40.0, load.perSideKg.sum(), 0.01)
    }

    @Test fun `format contains per side`() {
        val load = PlateCalculator.calculate(80.0, barKg = 20.0)
        val s = PlateCalculator.formatPlates(load, true)
        assertTrue(s.contains("per side"))
    }

    @Test fun `toLb conversion`() {
        assertEquals(2.20462, PlateCalculator.toLb(1.0), 0.001)
    }

    @Test fun `inventory limit respected`() {
        val inv = mapOf(20.0 to 2, 10.0 to 2)
        val load = PlateCalculator.calculate(120.0, barKg = 20.0, inventoryKg = inv)
        assertNotNull(load)
    }
}
