package com.repforge.core.model

import kotlinx.serialization.Serializable

@Serializable
data class GymProfile(
    val id: String,
    val name: String, // Home, My Gym, Hotel Gym, Outdoor Park
    val location: Location, // HOME/GYM/BOTH
    val environments: List<Environment> = listOf(Environment.GYM),
    val availableEquipment: Set<Equipment> = setOf(Equipment.BARBELL, Equipment.DUMBBELL, Equipment.MACHINE, Equipment.CABLE),
    val barWeightKg: Double = 20.0,
    val plateInventoryKg: Map<Double, Int> = mapOf(20.0 to 4, 15.0 to 2, 10.0 to 4, 5.0 to 4, 2.5 to 4, 1.25 to 2), // per side? total inventory
    val dumbbellMaxKg: Double = 50.0,
    val hasBench: Boolean = true,
    val hasRack: Boolean = true,
    val hasPullupBar: Boolean = true,
    val notes: String? = null,
) {
    fun canDo(exercise: Exercise): Boolean {
        if (exercise.equipment == Equipment.BODYWEIGHT || exercise.equipment == Equipment.NONE) return true
        if (exercise.equipment == Equipment.BARBELL && !hasBench && exercise.muscleGroup == MuscleGroup.CHEST) return false
        if (exercise.equipment == Equipment.MACHINE && availableEquipment.none { it == Equipment.MACHINE }) return false
        if (exercise.equipment == Equipment.CABLE && availableEquipment.none { it == Equipment.CABLE }) return false
        if (exercise.equipment == Equipment.DUMBBELL && dumbbellMaxKg < 20) return false
        return exercise.equipment in availableEquipment || exercise.location == Location.HOME || exercise.location == Location.BOTH
    }
    companion object {
        val Home = GymProfile("home", "Home", Location.HOME, listOf(Environment.HOME), setOf(Equipment.BODYWEIGHT, Equipment.BAND, Equipment.DUMBBELL, Equipment.KETTLEBELL), dumbbellMaxKg = 25.0, hasBench = false, hasRack = false)
        val MyGym = GymProfile("gym", "My Gym", Location.GYM, listOf(Environment.GYM), setOf(Equipment.BARBELL, Equipment.DUMBBELL, Equipment.MACHINE, Equipment.CABLE, Equipment.SMITH, Equipment.BODYWEIGHT), dumbbellMaxKg = 50.0, hasBench = true, hasRack = true)
        val Hotel = GymProfile("hotel", "Hotel Gym", Location.BOTH, listOf(Environment.GYM, Environment.HOME), setOf(Equipment.DUMBBELL, Equipment.BODYWEIGHT, Equipment.BAND), dumbbellMaxKg = 25.0, hasBench = true, hasRack = false, plateInventoryKg = mapOf(10.0 to 2, 5.0 to 2))
        val Outdoor = GymProfile("outdoor", "Outdoor Park", Location.HOME, listOf(Environment.OUTDOOR), setOf(Equipment.BODYWEIGHT, Equipment.BAND), hasBench = false, hasPullupBar = true)
    }
}

fun List<Exercise>.filterFor(gym: GymProfile): List<Exercise> = filter { gym.canDo(it) }
fun Routine.adaptTo(gym: GymProfile, allExercises: List<Exercise>): List<RoutineExercise> = emptyList() // TODO: map each RoutineExercise to substitute if unavailable
