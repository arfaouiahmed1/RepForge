package com.repforge.core.designsystem.token

import androidx.compose.ui.graphics.Color

/**
 * RepForge expressive palette — forged steel + ember + warm paper.
 *
 * Material 3 Expressive is tonal and high-contrast, not "one accent + grays".
 * Light: warm paper (#FFF8F4) with charcoal ink, ember is burned orange-red.
 * Dark: forged charcoal with glowing ember. Steel is vibrant, brass is surprise tertiary.
 */
object RepForgeColors {
    // Ember — molten metal. Burned orange-red, not iOS #FF3B30.
    val Ember10 = Color(0xFF3B0A00)
    val Ember20 = Color(0xFF5E1900)
    val Ember35 = Color(0xFF8B2E00)
    val Ember40 = Color(0xFF9C2A00) // kept for compat — maps to deep ember
    val Ember80 = Color(0xFFFFB59E) // dark primary — peach ember
    val Ember90 = Color(0xFFFFDBCF) // container — pale apricot
    val Ember95 = Color(0xFFFFEDE6)

    // Forge — charcoal with blue undertone
    val Forge10 = Color(0xFF1A1C1E)
    val Forge20 = Color(0xFF2F3133)
    val Forge30 = Color(0xFF454749)
    val Forge80 = Color(0xFFC5C6C9)
    val Forge90 = Color(0xFFE2E2E5)
    val Forge95 = Color(0xFFF1F0F3)
    val Forge98 = Color(0xFFF8F9FC)
    val Forge99 = Color(0xFFFCFCFF)

    // Paper — warm, slightly ochre. Not cold #FFFBFE.
    val Paper98 = Color(0xFFFFF8F4) // background light — warm paper
    val Paper95 = Color(0xFFFFEDE6)
    val Paper90 = Color(0xFFF5DDD6) // ember-tinted surfaceVariant

    // Steel — secondary. Vibrant.
    val Steel30 = Color(0xFF004B73)
    val Steel40 = Color(0xFF005F9E)
    val Steel80 = Color(0xFF8FCCFF)
    val Steel90 = Color(0xFFC9E6FF)
    val Steel95 = Color(0xFFE7F2FF)

    // Brass / Oxide — tertiary surprise
    val Brass40 = Color(0xFF6B5E00)
    val Brass80 = Color(0xFFDCC64E)
    val Brass90 = Color(0xFFFFEFB0)

    // Semantic — RIR / readiness
    val Success = Color(0xFF0E5A35)
    val SuccessContainer = Color(0xFFA6F2C0)
    val Warning = Color(0xFF7A4D00)
    val WarningContainer = Color(0xFFFFDDB3)
    val Error = Color(0xFF93000A)
    val ErrorContainer = Color(0xFFFFDAD6)

    // Expressive surfaces — immersive modes
    val RestSurfaceLight = Color(0xFF1A1C1E)
    val RestOnSurfaceLight = Color(0xFFF1F0F3)
    val WorkoutHeroLight = Color(0xFFFFDBCF)
}

object RepForgeSemantic {
    val ReadyHigh = Color(0xFF0E5A35)
    val ReadyHighContainer = Color(0xFFA6F2C0)
    val ReadyMedium = Color(0xFF7A4D00)
    val ReadyMediumContainer = Color(0xFFFFDDB3)
    val ReadyLow = Color(0xFF93000A)
    val ReadyLowContainer = Color(0xFFFFDAD6)
}
