package com.repforge.core.designsystem.icon

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.repforge.core.designsystem.token.RepForgeMotion

/**
 * RepForge expressive icons — SVG-animated via Compose Canvas + spring.
 * No XML ADVs: fully procedural, tintable, morphable.
 * Each icon supports `selected` morph (scale + stroke weight + ROND-ish roundedness).
 */

enum class RepForgeNavIcon { TODAY, PLAN, PROGRESS, LAB, PROFILE }

@Composable
fun RepForgeNavIconView(
    icon: RepForgeNavIcon,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier.size(24.dp),
) {
    val transition = rememberInfiniteTransition(label = "iconIdle")
    // Subtle idle bob when selected — expressive, not cocaine
    val bob by transition.animateFloat(
        initialValue = 0f, targetValue = if (selected) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(1600, easing = RepForgeMotion.ExpressiveEasing), RepeatMode.Reverse),
        label = "bob"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.12f else 1f,
        animationSpec = RepForgeMotion.BouncySpring as FiniteAnimationSpec<Float>,
        label = "iconScale"
    )
    Canvas(modifier = modifier) {
        val s = scale + bob * 0.04f
        drawWithScale(s) {
            when (icon) {
                RepForgeNavIcon.TODAY -> drawToday(color, selected)
                RepForgeNavIcon.PLAN -> drawPlan(color, selected)
                RepForgeNavIcon.PROGRESS -> drawProgress(color, selected)
                RepForgeNavIcon.LAB -> drawLab(color, selected)
                RepForgeNavIcon.PROFILE -> drawProfile(color, selected)
            }
        }
    }
}

private fun DrawScope.drawWithScale(scale: Float, block: DrawScope.() -> Unit) {
    val cx = size.width / 2; val cy = size.height / 2
    drawContext.canvas.save()
    drawContext.canvas.scale(scale, scale, cx, cy)
    block()
    drawContext.canvas.restore()
}

private fun DrawScope.drawToday(color: Color, selected: Boolean) {
    val w = size.width; val h = size.height
    val stroke = if (selected) 2.6f else 2.0f
    // Calendar / sun: rounded rect + sun burst when selected
    drawRoundRect(color, topLeft = Offset(w*0.15f, h*0.22f), size = Size(w*0.70f, h*0.62f), cornerRadius = CornerRadius(w*0.12f), style = Stroke(stroke))
    drawLine(color, Offset(w*0.15f, h*0.38f), Offset(w*0.85f, h*0.38f), strokeWidth = stroke)
    // two pins
    drawCircle(color, radius = 3f, center = Offset(w*0.33f, h*0.20f))
    drawCircle(color, radius = 3f, center = Offset(w*0.67f, h*0.20f))
    if (selected) {
        // sun dot inside
        drawCircle(color, radius = w*0.10f, center = Offset(w*0.5f, h*0.60f), style = Stroke(stroke))
        drawLine(color, Offset(w*0.5f, h*0.46f), Offset(w*0.5f, h*0.40f), strokeWidth = stroke)
    }
}

private fun DrawScope.drawPlan(color: Color, selected: Boolean) {
    val w = size.width; val h = size.height; val stroke = if (selected) 2.6f else 2.0f
    // Stacked layers
    drawRoundRect(color, Offset(w*0.20f, h*0.18f), Size(w*0.60f, h*0.18f), CornerRadius(w*0.08f), style = Stroke(stroke))
    drawRoundRect(color, Offset(w*0.15f, h*0.40f), Size(w*0.70f, h*0.18f), CornerRadius(w*0.08f), style = Stroke(stroke))
    drawRoundRect(color, Offset(w*0.20f, h*0.62f), Size(w*0.60f, h*0.18f), CornerRadius(w*0.08f), style = Stroke(stroke))
    if (selected) {
        // check on middle
        val p = Path().apply { moveTo(w*0.38f, h*0.50f); lineTo(w*0.46f, h*0.56f); lineTo(w*0.64f, h*0.44f) }
        drawPath(p, color, style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    }
}

private fun DrawScope.drawProgress(color: Color, selected: Boolean) {
    val w = size.width; val h = size.height; val stroke = if (selected) 2.6f else 2.0f
    // Sparkline + axis
    drawLine(color, Offset(w*0.12f, h*0.78f), Offset(w*0.88f, h*0.78f), strokeWidth = stroke)
    drawLine(color, Offset(w*0.12f, h*0.78f), Offset(w*0.12f, h*0.18f), strokeWidth = stroke)
    val p = Path().apply {
        moveTo(w*0.18f, h*0.62f); lineTo(w*0.32f, h*0.52f); lineTo(w*0.46f, h*0.58f); lineTo(w*0.60f, h*0.36f); lineTo(w*0.74f, h*0.42f); lineTo(w*0.86f, h*0.28f)
    }
    drawPath(p, color, style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
    // dots
    listOf(Offset(w*0.18f,h*0.62f), Offset(w*0.46f,h*0.58f), Offset(w*0.74f,h*0.42f), Offset(w*0.86f,h*0.28f)).forEach { drawCircle(color, 2.5f, it) }
}

private fun DrawScope.drawLab(color: Color, selected: Boolean) {
    val w = size.width; val h = size.height; val stroke = if (selected) 2.6f else 2.0f
    // Beaker: trapezoid + top bar
    val p = Path().apply {
        moveTo(w*0.28f, h*0.18f); lineTo(w*0.72f, h*0.18f); lineTo(w*0.80f, h*0.82f); lineTo(w*0.20f, h*0.82f); close()
    }
    drawPath(p, color, style = Stroke(stroke))
    drawLine(color, Offset(w*0.20f, h*0.18f), Offset(w*0.80f, h*0.18f), strokeWidth = stroke)
    // liquid
    drawRoundRect(color.copy(alpha = 0.28f), Offset(w*0.26f, h*0.55f), Size(w*0.48f, h*0.20f), CornerRadius(4f))
    if (selected) {
        // bubbles
        drawCircle(color, 2.2f, Offset(w*0.48f, h*0.48f))
        drawCircle(color, 1.8f, Offset(w*0.58f, h*0.40f))
        drawCircle(color, 1.5f, Offset(w*0.42f, h*0.36f))
    }
}

private fun DrawScope.drawProfile(color: Color, selected: Boolean) {
    val w = size.width; val h = size.height; val stroke = if (selected) 2.6f else 2.0f
    // Head + shoulders
    drawCircle(color, radius = w*0.18f, center = Offset(w*0.5f, h*0.34f), style = Stroke(stroke))
    val p = Path().apply {
        moveTo(w*0.18f, h*0.84f); cubicTo(w*0.18f, h*0.60f, w*0.30f, h*0.52f, w*0.50f, h*0.52f)
        cubicTo(w*0.70f, h*0.52f, w*0.82f, h*0.60f, w*0.82f, h*0.84f)
    }
    drawPath(p, color, style = Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round))
    if (selected) {
        // badge dot
        drawCircle(color, 3.2f, Offset(w*0.70f, h*0.22f))
    }
}

// Generic expressive icons for screens

@Composable
fun AnimatedDumbbellIcon(
    modifier: Modifier = Modifier.size(28.dp),
    tint: Color,
    animating: Boolean = false,
) {
    val lift by animateFloatAsState(
        targetValue = if (animating) 1f else 0f,
        animationSpec = if (animating) tween(380, easing = RepForgeMotion.ExpressiveEasing) else snap(),
        label = "lift"
    )
    Canvas(modifier) {
        val w = size.width; val h = size.height
        val dy = -lift * h * 0.18f
        // bar
        drawRoundRect(tint, Offset(w*0.18f, h*0.44f+dy), Size(w*0.64f, h*0.12f), CornerRadius(h*0.06f))
        // left plates
        drawRoundRect(tint, Offset(w*0.10f, h*0.30f+dy), Size(w*0.12f, h*0.40f), CornerRadius(4f))
        drawRoundRect(tint, Offset(w*0.02f, h*0.34f+dy), Size(w*0.08f, h*0.32f), CornerRadius(4f))
        // right plates mirrored
        drawRoundRect(tint, Offset(w*0.78f, h*0.30f+dy), Size(w*0.12f, h*0.40f), CornerRadius(4f))
        drawRoundRect(tint, Offset(w*0.90f, h*0.34f+dy), Size(w*0.08f, h*0.32f), CornerRadius(4f))
    }
}

@Composable
fun AnimatedCheckIcon(modifier: Modifier = Modifier.size(22.dp), tint: Color, checked: Boolean) {
    val progress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(420, easing = RepForgeMotion.ExpressiveEasing),
        label = "check"
    )
    Canvas(modifier) {
        val w = size.width; val h = size.height
        // circle background
        drawCircle(tint.copy(alpha = 0.18f * progress), radius = w*0.48f, center = center)
        drawCircle(tint, radius = w*0.48f, center = center, style = Stroke(2.2f))
        if (progress > 0.05f) {
            val p = Path().apply {
                moveTo(w*0.30f, h*0.54f); lineTo(w*0.44f, h*0.68f); lineTo(w*0.74f, h*0.36f)
            }
            drawPath(p, tint, style = Stroke(2.4f, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round))
        }
    }
}

@Composable
fun ExpressiveGhostIcon(modifier: Modifier = Modifier.size(20.dp), tint: Color) {
    Canvas(modifier) {
        val w = size.width; val h = size.height
        // ghost shape
        val p = Path().apply {
            moveTo(w*0.30f, h*0.85f); lineTo(w*0.30f, h*0.40f)
            cubicTo(w*0.30f, h*0.20f, w*0.38f, h*0.10f, w*0.50f, h*0.10f)
            cubicTo(w*0.62f, h*0.10f, w*0.70f, h*0.20f, w*0.70f, h*0.40f)
            lineTo(w*0.70f, h*0.85f)
            lineTo(w*0.62f, h*0.75f); lineTo(w*0.54f, h*0.85f); lineTo(w*0.46f, h*0.75f); lineTo(w*0.38f, h*0.85f); close()
        }
        drawPath(p, tint.copy(alpha = 0.9f))
        // eyes
        drawCircle(Color.White, 1.6f, Offset(w*0.42f, h*0.40f))
        drawCircle(Color.White, 1.6f, Offset(w*0.58f, h*0.40f))
    }
}
