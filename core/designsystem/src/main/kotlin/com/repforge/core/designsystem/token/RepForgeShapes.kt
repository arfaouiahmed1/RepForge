package com.repforge.core.designsystem.token

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Shape families — not one radius everywhere. Contrast is expressive.
 * @see https://m3.material.io/styles/shape/shape-scale-tokens
 */
object RepForgeShapes {
    val Hero: Shape = RoundedCornerShape(topStart = 56.dp, topEnd = 12.dp, bottomStart = 16.dp, bottomEnd = 44.dp)
    val HeroInverse: Shape = RoundedCornerShape(topStart = 12.dp, topEnd = 56.dp, bottomStart = 44.dp, bottomEnd = 16.dp)
    val HeroSquircle: Shape = RoundedCornerShape(48.dp)

    val PrimaryAction: Shape = RoundedCornerShape(28.dp)
    val PrimaryActionCircle: Shape = CircleShape
    val PrimaryActionSquircle: Shape = RoundedCornerShape(36.dp)
    val Pill: Shape = RoundedCornerShape(percent = 50)

    val Metric: Shape = RoundedCornerShape(24.dp)
    val MetricSmall: Shape = RoundedCornerShape(18.dp)
    val MetricPill: Shape = RoundedCornerShape(100.dp)

    val Media: Shape = RoundedCornerShape(topStart = 36.dp, topEnd = 12.dp, bottomEnd = 32.dp, bottomStart = 12.dp)
    val MediaAlt: Shape = RoundedCornerShape(topStart = 12.dp, topEnd = 36.dp, bottomStart = 32.dp, bottomEnd = 12.dp)

    val Sheet: Shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
    val SheetLarge: Shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)

    val NavPill: Shape = RoundedCornerShape(32.dp)
    val NavBar: Shape = RoundedCornerShape(28.dp)

    val Timer: Shape = RoundedCornerShape(40.dp)
    val TimerPulse: Shape = RoundedCornerShape(44.dp)

    val CardLarge: Shape = RoundedCornerShape(32.dp)
    val CardMedium: Shape = RoundedCornerShape(24.dp)
    val CardSmall: Shape = RoundedCornerShape(20.dp)
}
