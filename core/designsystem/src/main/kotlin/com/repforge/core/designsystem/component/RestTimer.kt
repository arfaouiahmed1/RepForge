package com.repforge.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles
import kotlinx.coroutines.delay

/**
 * RestTimer — becomes the visual hero. Not another card.
 * Organic timer shape -> subtle pulse -> haptic -> morphs into NEXT SET.
 */
@Composable
fun RestTimer(
    totalSeconds: Int,
    remainingSeconds: Int,
    onAddTime: (Int) -> Unit,
    onSkip: () -> Unit,
    onTimeUp: () -> Unit,
    modifier: Modifier = Modifier,
    nextExercise: String? = null,
    nextLoad: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    val progress = 1f - (remainingSeconds.toFloat() / totalSeconds.coerceAtLeast(1).toFloat())
    val infiniteTransition = rememberInfiniteTransition(label = "restPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.03f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds <= 0) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onTimeUp()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RepForgeShapes.Timer)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 22.dp, vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text("REST", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f))
        Box(
            modifier = Modifier
                .clip(RepForgeShapes.TimerPulse)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 36.dp, vertical = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            Text(
                text = String.format("%02d:%02d", mins, secs),
                style = RepForgeTypeRoles.MetricLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Text("remaining", style = RepForgeTypeRoles.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RestChip("-15", onClick = { onAddTime(-15) })
            RestChip("SKIP", onClick = onSkip, emphasized = true)
            RestChip("+15", onClick = { onAddTime(15) })
        }

        if (nextExercise != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("NEXT", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.56f))
            Text(nextExercise, style = RepForgeTypeRoles.HeadlineLoud, color = MaterialTheme.colorScheme.onSurface)
            if (nextLoad != null) Text(nextLoad, style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f))
        }
    }
}

@Composable
private fun RestChip(label: String, onClick: () -> Unit, emphasized: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RepForgeShapes.Pill)
            .background(if (emphasized) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = RepForgeTypeRoles.LabelExpressive,
            color = if (emphasized) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
