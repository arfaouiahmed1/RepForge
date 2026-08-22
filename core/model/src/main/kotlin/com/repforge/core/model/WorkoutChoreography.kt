package com.repforge.core.model

enum class EquipmentStatus { FREE, OCCUPIED, RESERVED }

data class ChoreographyAction(
    val type: Type, val fromIndex: Int, val toIndex: Int? = null, val supersetGroup: String? = null
) {
    enum class Type { REORDER, SUPERSET, CIRCUIT, SKIP, ADD, MARK_OCCUPIED }
}

object WorkoutChoreography {
    fun reorder(exercises: List<RoutineExercise>, from: Int, to: Int): List<RoutineExercise> {
        val m = exercises.toMutableList()
        val item = m.removeAt(from)
        m.add(to, item)
        return m.mapIndexed { i, e -> e.copy(position = i) }
    }
    fun groupSuperset(exercises: List<RoutineExercise>, indices: List<Int>, groupId: String): List<RoutineExercise> {
        // In UI, morph superset container with expressive motion
        return exercises.map { if (it.position in indices) it.copy(notes = "superset:$groupId") else it }
    }
}
