package com.repforge.core.designsystem.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.repforge.core.designsystem.token.RepForgeMotion
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

/**
 * MorphingSetButton — the portfolio showcase interaction.
 * Press: compresses -> expands -> checkmark -> row moves upward -> rest timer emerges.
 * Implements spec: button press -> control compresses -> shape expands -> checkmark -> completed row moves -> rest timer
 */
@Composable
fun MorphingSetButton(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isCompleted: Boolean = false,
) {
    val haptics = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = RepForgeMotion.MorphSpring,
        label = "morphScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RepForgeShapes.PrimaryActionSquircle)
            .background(if (isCompleted) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary)
            .clickable(enabled = enabled && !isCompleted) {
                pressed = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onComplete()
            }
            .animateContentSize(animationSpec = RepForgeMotion.standard())
            .padding(horizontal = 36.dp, vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Text("✓ COMPLETED", style = RepForgeTypeRoles.LabelExpressive, color = MaterialTheme.colorScheme.onSecondaryContainer)
        } else {
            Text("COMPLETE SET", style = RepForgeTypeRoles.LabelExpressive.copy(fontWeight = FontWeight.W800), color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
fun GiantStartShape(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "START",
) {
    val haptics = LocalHapticFeedback.current
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = RepForgeMotion.BouncySpring,
        label = "giantScale"
    )
    Box(
        modifier = modifier
            .size(196.dp)
            .scale(scale)
            .clip(RepForgeShapes.PrimaryActionCircle)
            .background(MaterialTheme.colorScheme.primary)
            .clickable {
                pressed = true
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = RepForgeTypeRoles.LabelExpressive.copy(fontWeight = FontWeight.W900, letterSpacing = 0.8.sp),
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}
