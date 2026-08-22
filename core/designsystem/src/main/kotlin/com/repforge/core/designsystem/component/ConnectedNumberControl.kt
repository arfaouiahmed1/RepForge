package com.repforge.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

/**
 * ConnectedNumberControl — grouped controls for weight/reps as per M3 expressive button groups.
 * Tap 82.5 kg reveals: -5, -2.5, +2.5, +5. Long-press for keyboard.
 */
@Composable
fun ConnectedNumberControl(
    value: String,
    unit: String,
    onDecrement: (Float) -> Unit,
    onIncrement: (Float) -> Unit,
    modifier: Modifier = Modifier,
    onLongPress: (() -> Unit)? = null,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = value, style = RepForgeTypeRoles.MetricLarge, color = MaterialTheme.colorScheme.onBackground)
            Text(text = unit.uppercase(), style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            ControlChip(label = "−5", onClick = { onDecrement(5f) })
            ControlChip(label = "−2.5", onClick = { onDecrement(2.5f) })
            Spacer(modifier = Modifier.width(6.dp))
            ControlChip(label = "+2.5", onClick = { onIncrement(2.5f) }, emphasized = true)
            ControlChip(label = "+5", onClick = { onIncrement(5f) }, emphasized = true)
        }
        Text(
            text = "long-press for keyboard",
            style = RepForgeTypeRoles.LabelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.52f)
        )
    }
}

@Composable
private fun ControlChip(label: String, onClick: () -> Unit, emphasized: Boolean = false) {
    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(RepForgeShapes.Pill)
            .background(if (emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(onClick = onClick)
            .padding(horizontal = 17.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = RepForgeTypeRoles.LabelExpressive, color = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun RepsControl(
    reps: Int,
    onRepsChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        ControlChip(label = "−", onClick = { if (reps > 1) onRepsChange(reps - 1) })
        Text(text = "× $reps reps", style = RepForgeTypeRoles.MetricMedium, color = MaterialTheme.colorScheme.onBackground)
        ControlChip(label = "+", onClick = { onRepsChange(reps + 1) })
    }
}
