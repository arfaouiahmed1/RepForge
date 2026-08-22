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
import com.repforge.core.model.SetLog
import com.repforge.core.model.estimated1RM
import kotlin.math.roundToInt

data class GhostSet(val today: SetLog?, val last: SetLog?)
data class GhostDelta(val volumePct: Double, val prCount: Int, val improved: Int, val regressed: Int)

fun computeGhostDelta(today: List<SetLog>, last: List<SetLog>): GhostDelta {
    val volToday = today.sumOf { it.weightKg * it.completedReps }
    val volLast = last.sumOf { it.weightKg * it.completedReps }
    val pct = if (volLast == 0.0) 0.0 else (volToday - volLast) / volLast * 100
    val prs = today.count { t -> last.none { it.weightKg >= t.weightKg && it.completedReps >= t.completedReps } && t.completedReps >= t.targetReps }
    val improved = today.count { t -> last.find { it.exerciseId == t.exerciseId }?.let { it.weightKg < t.weightKg || it.completedReps < t.completedReps } == true }
    val regressed = today.count { t -> last.find { it.exerciseId == t.exerciseId }?.let { it.weightKg > t.weightKg } == true }
    return GhostDelta(pct, prs, improved, regressed)
}

@Composable
fun GhostSetRow(ghost: GhostSet, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth().clip(RepForgeShapes.CardMedium).background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text("TODAY", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            Text(ghost.today?.let { "${it.weightKg} × ${it.completedReps}" } ?: "—", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
            ghost.today?.let { Text("est 1RM ${estimated1RM(it.weightKg, it.completedReps).roundToInt()}kg", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.primary) }
        }
        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
            Text("LAST TIME", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Text(ghost.last?.let { "${it.weightKg} × ${it.completedReps}" } ?: "—", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
            // Ghost progress indicator — subtle
            Box(modifier = Modifier.width(80.dp).height(4.dp).clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.surface)) {
                val pct = if ((ghost.last?.weightKg ?: 0.0) == 0.0) 0f else ((ghost.today?.weightKg ?: 0.0) / (ghost.last?.weightKg ?: 1.0)).toFloat().coerceIn(0f, 1f)
                Box(modifier = Modifier.fillMaxWidth(pct).height(4.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
            }
        }
    }
}

@Composable
fun GhostCompletionSummary(delta: GhostDelta, modifier: Modifier = Modifier) {
    Column(modifier = modifier.clip(RepForgeShapes.Hero).background(MaterialTheme.colorScheme.primaryContainer).padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("VS LAST TIME", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text("${if (delta.volumePct >= 0) "+" else ""}${String.format("%.1f", delta.volumePct)}% volume", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text("${delta.prCount} PRs • ${delta.improved} sets improved • ${delta.regressed} regressed", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
    }
}
