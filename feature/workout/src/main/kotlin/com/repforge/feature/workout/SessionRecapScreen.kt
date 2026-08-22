package com.repforge.feature.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import com.repforge.core.model.SessionRecap

@Composable
fun SessionRecapScreen(recap: SessionRecap, onShare: () -> Unit, onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("WORKOUT COMPLETE", style = RepForgeTypeRoles.DisplayHero, color = MaterialTheme.colorScheme.onBackground)
        // Dominant visual — not sad modal
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RepForgeShapes.Hero).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("${recap.durationMin} MIN • ${recap.totalVolumeKg.toInt()} KG", style = RepForgeTypeRoles.MetricLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f).clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Column { Text("PRs", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)); Text("${recap.prs.size}", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface) }
            }
            Box(Modifier.weight(1f).clip(RepForgeShapes.Metric).background(MaterialTheme.colorScheme.surfaceVariant).padding(16.dp)) {
                Column { Text("VOLUME Δ", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)); Text("${if (recap.volumeDeltaPct>=0) "+" else ""}${"%.1f".format(recap.volumeDeltaPct)}%", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onSurface) }
            }
        }
        Text("Strongest: ${recap.strongestSet?.let { "${it.exerciseName} ${it.weightKg}×${it.completedReps}" } ?: "—"} • ${recap.exercisesDone} exercises • ${recap.consistencyPct}% adherence", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Muscle: ${recap.muscleDistribution.entries.joinToString { "${it.key.name} ${it.value}%" }}", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        Text("Shareable card: BENCH 100KG · NEW PR with 3D silhouette", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
    }
}
