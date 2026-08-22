package com.repforge.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

@Composable
fun WorkoutToolbar(
    title: String,
    progress: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RepForgeShapes.MetricSmall)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title.uppercase(), style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurface)
        Text(progress, style = RepForgeTypeRoles.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f))
    }
}

@Composable
fun SetRow(
    index: Int,
    load: String,
    reps: String,
    rir: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RepForgeShapes.MetricSmall)
            .background(if (isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (isCompleted) "✓ $index" else "$index", style = RepForgeTypeRoles.LabelExpressive, color = if (isCompleted) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface)
        Text(load, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
        Text(reps, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurface)
        Text(rir, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f))
    }
}
