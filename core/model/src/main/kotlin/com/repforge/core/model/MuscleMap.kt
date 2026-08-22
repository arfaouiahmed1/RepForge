package com.repforge.core.model

// Front/back 3D human — tap muscle → ranked exercises by available equipment. After workout, approx distribution (no 71.4% deltoid nonsense)
data class MuscleMapSelection(val muscle: MuscleGroup, val exercises: List<Exercise>)
object MuscleMap {
    fun exercisesFor(muscle: MuscleGroup, gym: GymProfile, all: List<Exercise>): List<Exercise> =
        all.filter { it.primaryMuscles.contains(muscle) || it.muscleGroup == muscle }.filter { gym.canDo(it) }.sortedByDescending { it.compound }
    fun distribution(lastWorkout: List<SetLog>): Map<MuscleGroup, Int> {
        val byMuscle = lastWorkout.groupBy { it.exerciseId }
        // Approximate — group sets per muscle, never claim precise activation %
        return emptyMap() // TODO: map exerciseId → MuscleGroup via ExerciseDao
    }
}
