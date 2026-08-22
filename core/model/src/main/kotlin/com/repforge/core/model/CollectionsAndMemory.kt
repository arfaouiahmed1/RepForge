package com.repforge.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ExerciseCollection(val id: String, val name: String, val exerciseIds: List<String>, val isSystem: Boolean = false) {
    companion object {
        val Favorites = ExerciseCollection("fav", "Favorites", emptyList(), true)
        val MyGym = ExerciseCollection("mygym", "My gym", emptyList(), true)
        val Home = ExerciseCollection("home", "Home", emptyList(), true)
        val Avoid = ExerciseCollection("avoid", "Exercises I avoid", emptyList(), true)
        val WantToTry = ExerciseCollection("want", "Want to try", emptyList(), true)
    }
}

@Serializable
data class ExerciseNote(val exerciseId: String, val text: String, val updatedAt: Long) // e.g. Seat notch 4, Cable 7, Use blue bench
@Serializable
data class EquipmentMemory(val exerciseId: String, val key: String, val value: String, val updatedAt: Long) // machine seat 4, pin 7, rack height 3

// Custom tracking modes for unicorn curl
@Serializable
enum class CustomTracking { WEIGHT_REPS, REPS_WEIGHTED, TIME, DISTANCE, ASSISTED }
@Serializable
data class CustomExercise(val id: String, val name: String, val tracking: CustomTracking, val muscle: MuscleGroup)
