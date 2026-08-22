package com.repforge.core.model

data class WarmupSet(val weightKg: Double, val reps: Int, val isWarmup: Boolean = true)

object WarmupBuilder {
    // Deterministic ramp — 20×10 → 40×5 → 60×3 → 72.5×1 → 82.5 working. Prefs: extra sets or minimal
    fun build(targetKg: Double, recentWorking: List<SetLog> = emptyList(), minimal: Boolean = false): List<WarmupSet> {
        if (targetKg <= 20) return emptyList()
        val sets = mutableListOf<WarmupSet>()
        // Base jumps from 20kg bar
        if (targetKg >= 40) sets.add(WarmupSet(20.0, 10))
        if (targetKg >= 60 && !minimal) sets.add(WarmupSet(40.0, 5))
        if (targetKg >= 70) sets.add(WarmupSet((targetKg * 0.6).let { (it/5).toInt()*5.0 }, 3))
        if (targetKg >= 80) sets.add(WarmupSet((targetKg * 0.85).let { (it/2.5).toInt()*2.5 }, 1))
        // If target is heavy, add intermediate
        if (targetKg >= 100 && !minimal) sets.add(2, WarmupSet((targetKg * 0.75).let { (it/5).toInt()*5.0 }, 2))
        return sets.distinctBy { it.weightKg }
    }
}

enum class SetType { WARMUP, WORKING, DROP, REST_PAUSE, AMRAP, EMOM, TIMED, UNILATERAL_LEFT, UNILATERAL_RIGHT, ASSISTED }

data class AdvancedSet(
    val base: SetLog,
    val type: SetType = SetType.WORKING,
    val supersetId: String? = null, // group id for superset/circuit
    val dropToKg: Double? = null,
    val timeSec: Int? = null, // for TIMED/EMOM
    val unilateralSide: String? = null,
)
