package com.repforge.core.designsystem.token

import android.animation.TimeInterpolator
import android.view.animation.PathInterpolator
import androidx.compose.ui.unit.dp

object RepForgeBrandTokens {
    val MarkCompact = 72.dp
    val MarkLoading = 112.dp
    val MarkHero = 144.dp
    val SplashLift = 8.dp
    val RevealLift = 24.dp
    val OnboardingMaxWidth = 720.dp
    val ExpandedBreakpoint = 600.dp
    val PrimaryActionHeight = 56.dp

    const val SplashExitScale = 1.08f
    const val RevealStartScale = 0.88f
    const val LoadingStartScale = 0.96f
    const val LoadingStartAlpha = 0.72f

    val SplashExitInterpolator: TimeInterpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
}
