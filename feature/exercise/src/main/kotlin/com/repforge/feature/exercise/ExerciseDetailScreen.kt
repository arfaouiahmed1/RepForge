package com.repforge.feature.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.Exercise
import com.repforge.core.model.MuscleGroup
import com.repforge.core.model.Equipment
import com.repforge.core.threed.ExerciseModelViewer
import com.repforge.core.threed.MuscleModelViewer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor() : ViewModel()

@Composable
fun ExerciseDetailRoute(exerciseId: String, viewModel: ExerciseDetailViewModel = hiltViewModel()) {
    val ex = remember(exerciseId) {
        Exercise(id = exerciseId, name = "Barbell Bench Press", muscleGroup = MuscleGroup.CHEST, primaryMuscles = listOf(MuscleGroup.CHEST), secondaryMuscles = listOf(MuscleGroup.SHOULDERS, MuscleGroup.TRICEPS), equipment = Equipment.BARBELL, movementPattern = com.repforge.core.model.MovementPattern.HORIZONTAL_PUSH, modality = com.repforge.core.model.Modality.STRENGTH, description = "Compound chest push — primary pecs, secondary front delts + triceps.", instructions = listOf("Retract scapula, feet flat", "Lower to mid-chest, elbows 45°", "Drive through heels, lockout"), setupCues = listOf("Bench height: eyes under bar", "Grip: 1.5× shoulder width", "Feet flat, arch slight"), executionCues = listOf("Brace, unrack", "Descend 1.3s, bottom 1.6s, ascend 2.6s"), commonMistakes = listOf("Bar drifts over neck", "Elbows flare 90°"), glbAsset = "models/bench_press.glb", animationAsset = "models/bench_press.glb", thumbnailAsset = "models/bench_press.webp", defaultRestSeconds = 90)
    }
    ExerciseDetailScreen(exercise = ex)
}

@Composable
fun ExerciseDetailScreen(exercise: Exercise) {
    var mode by remember { mutableStateOf(DetailMode.MOTION) }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(exercise.name.uppercase(), style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground) }
        item {
            // ButtonGroup — Motion | Muscles | Setup with expand/shrink interaction (M3 ButtonGroup)
            // Using SegmentedButton for now; isolated in designsystem so can swap to ButtonGroup when stable
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                DetailMode.entries.forEachIndexed { idx, m ->
                    SegmentedButton(selected = mode == m, onClick = { mode = m }, shape = SegmentedButtonDefaults.itemShape(index = idx, count = 3), label = { Text(m.label) })
                }
            }
        }
        item {
            when (mode) {
                DetailMode.MOTION -> {
                    // Motion: 3D loop, swipe to rotate, tap to pause, drag timeline
                    ExerciseModelViewer(glbAsset = exercise.animationAsset, modifier = Modifier.fillMaxWidth().height(300.dp))
                    Text("Swipe to rotate • tap to pause • drag timeline (30fps, 3.5s loop)", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                }
                DetailMode.MUSCLES -> {
                    // Muscles: freeze in representative pose, highlight anatomy
                    MuscleModelViewer(exercise = exercise, modifier = Modifier.fillMaxWidth())
                    Column(modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("PRIMARY", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        exercise.primaryMuscles.forEach { Text("• ${it.name} — Primary mover", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface) }
                        if (exercise.secondaryMuscles.isNotEmpty()) {
                            Text("SECONDARY", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            exercise.secondaryMuscles.forEach { Text("• ${it.name}", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
                DetailMode.SETUP -> {
                    // Setup: camera shift, sparse overlay markers (rack height, grip, foot)
                    MuscleModelViewer(exercise = exercise, modifier = Modifier.fillMaxWidth())
                    Column(modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("SETUP", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        exercise.setupCues.forEach { Text("• $it", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface) }
                        Text("EXECUTION", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        exercise.executionCues.forEach { Text("• $it", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface) }
                        if (exercise.commonMistakes.isNotEmpty()) {
                            Text("AVOID", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.error)
                            exercise.commonMistakes.forEach { Text("• $it", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("QUICK STATS", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                Text("${exercise.muscleGroup.name} • ${exercise.equipments.joinToString("/")} • ${exercise.environments.joinToString("/")} • ${exercise.movementPattern.name} • ${exercise.trackingType.name}", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
                Text("Rest: ${exercise.defaultRestSeconds}s • Alternatives: ${exercise.alternatives.takeIf { it.isNotEmpty() } ?: listOf("—").joinToString()} • Source: ${exercise.source ?: "RepForge canonical"}", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
        }
    }
}

private enum class DetailMode(val label: String) { MOTION("Motion"), MUSCLES("Muscles"), SETUP("Setup") }
