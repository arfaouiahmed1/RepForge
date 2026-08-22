package com.repforge.core.model

import kotlin.math.roundToInt

data class PlateLoad(val perSideKg: List<Double>, val barKg: Double, val remainingKg: Double)

object PlateCalculator {
    fun calculate(targetKg: Double, barKg: Double = 20.0, inventoryKg: Map<Double, Int> = mapOf(20.0 to 4, 15.0 to 2, 10.0 to 4, 5.0 to 4, 2.5 to 4, 1.25 to 2), isKg: Boolean = true): PlateLoad {
        val target = if (isKg) targetKg else targetKg * 0.453592
        val bar = if (isKg) barKg else barKg * 0.453592
        var needPerSide = ((target - bar) / 2).coerceAtLeast(0.0)
        val sorted = inventoryKg.keys.sortedDescending()
        val perSide = mutableListOf<Double>()
        val remainingInventory = inventoryKg.toMutableMap()
        for (plate in sorted) {
            var count = remainingInventory[plate] ?: 0
            // inventory is per side? assume total/2
            val perSideAvailable = count / 2
            while (needPerSide >= plate - 1e-9 && perSideAvailable > perSide.count { it == plate }) {
                perSide.add(plate)
                needPerSide -= plate
            }
        }
        // Greedy without inventory limit fallback
        if (needPerSide > 0.001) {
            for (plate in sorted) {
                while (needPerSide >= plate - 1e-9) {
                    perSide.add(plate)
                    needPerSide -= plate
                    if (needPerSide < 0.001) break
                }
            }
        }
        perSide.sortDescending()
        return PlateLoad(perSide, bar, (needPerSide * 2).let { if (it < 0) 0.0 else it })
    }

    fun toLb(kg: Double) = kg * 2.20462
    fun formatPlates(load: PlateLoad, isKg: Boolean): String {
        if (load.perSideKg.isEmpty()) return if (load.remainingKg > 0.01) "Need ${(load.remainingKg).roundToInt()}kg more" else "Bar only"
        val unit = if (isKg) "kg" else "lb"
        val plates = load.perSideKg.groupingBy { it }.eachCount().entries.sortedByDescending { it.key }
            .joinToString(" + ") { (kg, n) -> "${n}×${if (isKg) kg else toLb(kg).roundToInt()}${unit}" }
        return "$plates per side (bar ${load.barKg}$unit)"
    }
}
