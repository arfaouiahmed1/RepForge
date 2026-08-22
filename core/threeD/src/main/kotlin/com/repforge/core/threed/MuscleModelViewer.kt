package com.repforge.core.threed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.Exercise

/**
 * 3D viewer — placeholder 2D for demo (Filament disabled to avoid Kotlin 2.4 mismatch).
 * Real: SurfaceView + ModelViewer + loadModelGlb. See ExerciseModelViewer for Filament code (commented).
 */
@Composable
fun MuscleModelViewer(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    autoRotate: Boolean = true,
) {
    val context = LocalContext.current
    val assetPath = exercise.glbAsset ?: exercise.animationAsset
    val hasAsset = remember(assetPath) {
        assetPath != null && (
            try { context.assets.open(assetPath).close(); true } catch (_: Exception) { false } ||
            ModelCache.getCachedFile(context, assetPath)?.exists() == true
        )
    }

    Box(
        modifier = modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // Real Filament commented to keep demo building on Kotlin 2.1. Uncomment when bumping to Kotlin 2.4 + filament 1.74
        // if (hasAsset) AndroidView(SurfaceView ...) { Utils.init(); ModelViewer(...).loadModelGlb(...) }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(16.dp)) {
            Text(exercise.name.uppercase(), style = RepForgeTypeRoles.HeadlineLoud, color = MaterialTheme.colorScheme.onSurface)
            Text(exercise.environments.joinToString("/") + " • " + exercise.muscleGroup.name + if (exercise.secondaryMuscles.isNotEmpty()) " • " + exercise.secondaryMuscles.joinToString(" • ") { it.name } else "", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Text("GLB: ${assetPath ?: "add to assets/models/"} • ${if (hasAsset) "found" else "placeholder"}", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MuscleChip(exercise.primaryMuscles.firstOrNull()?.name ?: exercise.muscleGroup.name, 0.9f, true)
                exercise.secondaryMuscles.take(2).forEach { MuscleChip(it.name, 0.5f) }
            }
            Text("2D fallback — add GLB + enable Filament to render mannequin + bar kit", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
        }
        if (hasAsset) {
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(exercise.primaryMuscles.firstOrNull()?.name ?: exercise.muscleGroup.name, style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MuscleChip((exercise.primaryMuscles.firstOrNull()?.name ?: exercise.muscleGroup.name), 0.9f, true)
                    exercise.secondaryMuscles.take(2).forEach { MuscleChip(it.name, 0.55f) }
                }
            }
        }
    }
}

@Composable
private fun MuscleChip(label: String, heat: Float, isPrimary: Boolean = false) {
    val bg = if (isPrimary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val fg = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(bg.copy(alpha = 0.6f + heat*0.4f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(label, style = RepForgeTypeRoles.LabelExpressive, color = fg)
    }
}
