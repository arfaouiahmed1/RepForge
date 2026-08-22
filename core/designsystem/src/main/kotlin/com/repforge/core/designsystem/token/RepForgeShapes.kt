package com.repforge.core.designsystem.token

import androidx.compose.ui.graphics.Shape

/**
 * Shape roles — not one radius everywhere. Contrast is expressive.
 *
 * Consolidated: every value now lives in [FitnessVisualTokens.FitnessShapes]
 * (built on the md.sys corner scale none→full in [FitnessVisualTokens.FitnessShapeScale]).
 * This object preserves the historical RepForge API consumed across feature modules;
 * each property delegates to the same single token instance, so instance identity
 * (asserted by RepForgeShapesTest) is preserved.
 *
 * @see https://m3.material.io/styles/shape/shape-scale-tokens
 */
object RepForgeShapes {
    val Hero: Shape get() = FitnessVisualTokens.FitnessShapes.hero
    val HeroInverse: Shape get() = FitnessVisualTokens.FitnessShapes.heroInverse
    val HeroSquircle: Shape get() = FitnessVisualTokens.FitnessShapes.heroSquircle

    val PrimaryAction: Shape get() = FitnessVisualTokens.FitnessShapes.compactAction
    val PrimaryActionCircle: Shape get() = FitnessVisualTokens.FitnessShapes.compactActionCircle
    val PrimaryActionSquircle: Shape get() = FitnessVisualTokens.FitnessShapes.compactActionSquircle
    val Pill: Shape get() = FitnessVisualTokens.FitnessShapes.pill

    val Metric: Shape get() = FitnessVisualTokens.FitnessShapes.achievement
    val MetricSmall: Shape get() = FitnessVisualTokens.FitnessShapes.achievementSmall
    val MetricPill: Shape get() = FitnessVisualTokens.FitnessShapes.achievementPill

    val Media: Shape get() = FitnessVisualTokens.FitnessShapes.media
    val MediaAlt: Shape get() = FitnessVisualTokens.FitnessShapes.mediaAlt

    val Sheet: Shape get() = FitnessVisualTokens.FitnessShapes.sheet
    val SheetLarge: Shape get() = FitnessVisualTokens.FitnessShapes.sheetLarge

    val NavPill: Shape get() = FitnessVisualTokens.FitnessShapes.navPill
    val NavBar: Shape get() = FitnessVisualTokens.FitnessShapes.navBar

    val Timer: Shape get() = FitnessVisualTokens.FitnessShapes.timer
    val TimerPulse: Shape get() = FitnessVisualTokens.FitnessShapes.timerPulse

    val CardLarge: Shape get() = FitnessVisualTokens.FitnessShapes.cardLarge
    val CardMedium: Shape get() = FitnessVisualTokens.FitnessShapes.cardMedium
    val CardSmall: Shape get() = FitnessVisualTokens.FitnessShapes.cardSmall
}
