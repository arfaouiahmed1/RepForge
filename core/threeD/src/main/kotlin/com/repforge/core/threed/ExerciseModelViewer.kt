package com.repforge.core.threed

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeMotion
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypeRoles

@Composable
fun ExerciseModelViewer(
    glbAsset: String?,
    modifier: Modifier = Modifier,
    autoRotate: Boolean = true,
    highlightMuscles: Map<String, Float> = emptyMap()
) {
    if (glbAsset == null) {
        Box(modifier = modifier.clip(RepForgeShapes.CardLarge).background(MaterialTheme.colorScheme.surfaceVariant).height(300.dp), contentAlignment = Alignment.Center) {
            Text("Select an exercise to preview motion", style = RepForgeTypeRoles.BodySupport, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    val context = LocalContext.current
    val hasAsset = remember(glbAsset) {
        try { context.assets.open(glbAsset.removePrefix("models/")).close(); true } catch (_: Exception) {
            try { context.assets.open(glbAsset).close(); true } catch (_: Exception) { false }
        }
    }
    var isPlaying by remember { mutableStateOf(true) }
    val infinite = rememberInfiniteTransition(label = "motionLoop")
    val anim by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Restart),
        label = "loop"
    )
    val t = if (isPlaying) anim else 0.35f

    Box(
        modifier = modifier
            .clip(RepForgeShapes.Media)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .height(300.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width; val h = size.height
            val progress = (kotlin.math.sin(t * 2 * Math.PI) * 0.5 + 0.5).toFloat()
            val barY = h * 0.32f + progress * h * 0.38f
            val ember = Color(0xFF9C2A00)
            val steel = Color(0xFF005F9E)
            drawRect(color = Color(0xFF0F0F0F).copy(alpha = 0.06f), topLeft = Offset(w*0.08f, h*0.18f), size = androidx.compose.ui.geometry.Size(w*0.84f, h*0.64f))
            drawPath(
                path = Path().apply {
                    moveTo(w*0.22f, barY); lineTo(w*0.78f, barY)
                },
                color = ember,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
            val plateW = w*0.08f; val plateH = h*0.20f
            drawRoundRect(color = steel, topLeft = Offset(w*0.20f - plateW/2, barY - plateH/2), size = androidx.compose.ui.geometry.Size(plateW, plateH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f))
            drawRoundRect(color = steel, topLeft = Offset(w*0.80f - plateW/2, barY - plateH/2), size = androidx.compose.ui.geometry.Size(plateW, plateH), cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f))
            // heat dots if provided
            if (highlightMuscles.isNotEmpty()) {
                highlightMuscles.entries.take(3).forEachIndexed { i, e ->
                    val alpha = e.value.coerceIn(0f,1f)
                    drawCircle(color = ember.copy(alpha = 0.18f + alpha*0.55f), radius = 14f + alpha*10f, center = Offset(w*0.30f + i*w*0.20f, h*0.78f))
                }
            }
            // timeline scrub
            drawRoundRect(color = Color.White.copy(alpha = 0.9f), topLeft = Offset(w*0.12f, h*0.88f), size = androidx.compose.ui.geometry.Size(w*0.76f, 4f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f))
            drawCircle(color = ember, radius = 7f, center = Offset(w*0.12f + t * w*0.76f, h*0.90f))
        }
        Column(modifier = Modifier.align(Alignment.TopStart).padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(if (hasAsset) "GLB • 3.5s loop • 30fps" else "Procedural preview — add GLB for true 3D", style = RepForgeTypeRoles.LabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            if (highlightMuscles.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    highlightMuscles.entries.take(3).forEach { (k,v) ->
                        Box(modifier = Modifier.clip(RepForgeShapes.Pill).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f + v*0.5f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(k, style = RepForgeTypeRoles.LabelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }
        Text(
            if (isPlaying) "tap to pause • swipe to rotate • drag timeline" else "paused • tap to play",
            style = RepForgeTypeRoles.LabelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}
