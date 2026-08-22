package com.repforge.core.designsystem.token

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

/**
 * Motion vocabulary — one expressive focal interaction per screen.
 * Uses Material MotionScheme when available (M3 1.4.0), fallback to these tokens.
 */
object RepForgeMotion {

    val ExpressiveEasing = CubicBezierEasing(0.34f, 0.80f, 0.34f, 1.0f)
    val ExpressiveDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val ExpressiveAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val EmphasizedEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val StandardEasing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    val SnapEasing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1.0f)

    const val DurationShort = 180
    const val DurationMedium = 380
    const val DurationLong = 560
    const val DurationExtraLong = 760

    val MorphSpring = spring<Float>(dampingRatio = 0.72f, stiffness = 280f)
    val BouncySpring = spring<Float>(dampingRatio = 0.55f, stiffness = 320f)
    val GentleSpring = spring<Float>(dampingRatio = 0.88f, stiffness = 180f)
    val SnappySpring = spring<Float>(dampingRatio = 0.60f, stiffness = 420f)
    val WobblySpring = spring<Float>(dampingRatio = 0.52f, stiffness = 260f)

    fun <T> expressiveTween(duration: Int = DurationMedium) = tween<T>(durationMillis = duration, easing = ExpressiveEasing)
    fun <T> emphasizedTween(duration: Int = DurationMedium) = tween<T>(durationMillis = duration, easing = ExpressiveDecelerate)
    fun <T> snapTween(duration: Int = DurationShort) = tween<T>(durationMillis = duration, easing = SnapEasing)

    fun <T> expressiveSpring() = MorphSpring as androidx.compose.animation.core.FiniteAnimationSpec<T>
}
