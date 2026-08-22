package com.repforge.core.designsystem.token

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * FitnessVisualTokens — THE single source of truth for RepForge's Material 3 Expressive
 * visual tokens. This is the ONLY file in core/designsystem permitted to contain raw
 * `Color(0x…)` values (md.sys palette definitions); every consumer — including
 * RepForgeTheme — must reference roles from here, never inline hex.
 *
 * Structure follows the MD3 spec (m3.material.io):
 *  - [FitnessPalette]   brand tonal palettes (ember/forge/paper/steel/brass) + semantic
 *  - [fitnessLightColorScheme] / [fitnessDarkColorScheme] full md.sys.color role mapping
 *  - [FitnessShapeScale] md.sys.shape.corner scale (none → full)
 *  - [FitnessShapes]     fitness shape roles (hero/media/compactAction/achievement/…)
 *  - [FitnessSpacing]    8dp spacing system (I/O 2026 expressive layout guidance)
 *
 * Tonal pairing rule (color-system.md): colors are only valid inside their intended
 * pairs (primary+onPrimary, container+onContainer, surface*+onSurface*). Never mix.
 */
object FitnessVisualTokens {

    // ------------------------------------------------------------------
    // Brand tonal palettes — forged steel + ember + warm paper.
    // Ember = burned orange-red primary. Forge = charcoal neutral w/ blue undertone.
    // Paper = warm ochre-tinted surface neutral. Steel = secondary. Brass = tertiary surprise.
    // ------------------------------------------------------------------
    object FitnessPalette {
        // Ember — molten metal primary (burned orange-red, not iOS #FF3B30)
        val Ember10 = Color(0xFF3B0A00)
        val Ember20 = Color(0xFF5E1900)
        val Ember30 = Color(0xFF7A2200)
        val Ember35 = Color(0xFF8B2E00)
        val Ember40 = Color(0xFF9C2A00) // light primary tone
        val Ember80 = Color(0xFFFFB59E) // dark primary tone
        val Ember90 = Color(0xFFFFDBCF) // container
        val Ember95 = Color(0xFFFFEDE6)

        // Forge — charcoal with blue undertone (neutral palette)
        val Forge10 = Color(0xFF1A1C1E)
        val Forge20 = Color(0xFF2F3133)
        val Forge30 = Color(0xFF454749)
        val Forge80 = Color(0xFFC5C6C9)
        val Forge90 = Color(0xFFE2E2E5)
        val Forge95 = Color(0xFFF1F0F3)
        val Forge98 = Color(0xFFF8F9FC)
        val Forge99 = Color(0xFFFCFCFF)

        // Paper — warm, slightly ochre surface neutral (not cold #FFFBFE)
        val Paper98 = Color(0xFFFFF8F4)
        val Paper95 = Color(0xFFFFEDE6)
        val Paper90 = Color(0xFFF5DDD6)

        // Steel — vibrant secondary
        val Steel10 = Color(0xFF001E30)
        val Steel20 = Color(0xFF00344F)
        val Steel30 = Color(0xFF004B73)
        val Steel40 = Color(0xFF005F9E)
        val Steel80 = Color(0xFF8FCCFF)
        val Steel90 = Color(0xFFC9E6FF)
        val Steel95 = Color(0xFFE7F2FF)

        // Brass / Oxide — tertiary surprise
        val Brass10 = Color(0xFF211B00)
        val Brass30 = Color(0xFF534600)
        val Brass40 = Color(0xFF6B5E00)
        val Brass80 = Color(0xFFDCC64E)
        val Brass90 = Color(0xFFFFEFB0)

        // Warm-neutral scheme-only tones (surface containers, outlines, inverse)
        val SurfaceContainerLight = Color(0xFFF8EEE8)
        val SurfaceContainerHighLight = Color(0xFFF2E6DF)
        val SurfaceContainerHighestLight = Color(0xFFEBDDD5)
        val SurfaceContainerLowLight = Color(0xFFFBF4F0)
        val SurfaceDimLight = Color(0xFFE1D7D2)
        val OutlineLight = Color(0xFF85736E)
        val OutlineVariantLight = Color(0xFFD7C2BC)
        val InverseSurfaceLight = Color(0xFF362F2C)
        val InverseOnSurfaceLight = Color(0xFFF9F1ED)

        val BackgroundDark = Color(0xFF201A18)
        val OnBackgroundDark = Color(0xFFEDE0DB)
        val SurfaceVariantDark = Color(0xFF53433F)
        val OnSurfaceVariantDark = Color(0xFFD7C2BC)
        val SurfaceContainerDark = Color(0xFF271E1C)
        val SurfaceContainerHighDark = Color(0xFF322823)
        val SurfaceContainerHighestDark = Color(0xFF3D2F2B)
        val SurfaceContainerLowDark = Color(0xFF1D1614)
        val SurfaceContainerLowestDark = Color(0xFF150F0E)
        val SurfaceBrightDark = Color(0xFF4B403C)
        val OutlineDark = Color(0xFFA08C87)
        val ErrorDark = Color(0xFFFFB4AB)
        val OnErrorDark = Color(0xFF690005)
        val OnErrorContainerLight = Color(0xFF410002)

        // Semantic — RIR / readiness (static, outside dynamic schemes)
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

    // ------------------------------------------------------------------
    // md.sys.color role mapping — full ColorScheme coverage for both themes.
    // Pairing is enforced by construction: each onX comes from the same palette.
    // ------------------------------------------------------------------

    /** Light scheme — warm paper surfaces, ember accents. */
    fun fitnessLightColorScheme(): ColorScheme = lightColorScheme(
        primary = FitnessPalette.Ember40,
        onPrimary = Color.White,
        primaryContainer = FitnessPalette.Ember90,
        onPrimaryContainer = FitnessPalette.Ember10,
        secondary = FitnessPalette.Steel40,
        onSecondary = Color.White,
        secondaryContainer = FitnessPalette.Steel90,
        onSecondaryContainer = FitnessPalette.Steel10,
        tertiary = FitnessPalette.Brass40,
        onTertiary = Color.White,
        tertiaryContainer = FitnessPalette.Brass90,
        onTertiaryContainer = FitnessPalette.Brass10,
        error = FitnessPalette.Error,
        onError = Color.White,
        errorContainer = FitnessPalette.ErrorContainer,
        onErrorContainer = FitnessPalette.OnErrorContainerLight,
        background = FitnessPalette.Paper98,
        onBackground = FitnessPalette.Forge10,
        surface = FitnessPalette.Paper98,
        onSurface = FitnessPalette.Forge10,
        surfaceVariant = FitnessPalette.Paper90,
        onSurfaceVariant = FitnessPalette.Forge20,
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = FitnessPalette.SurfaceContainerLowLight,
        surfaceContainer = FitnessPalette.SurfaceContainerLight,
        surfaceContainerHigh = FitnessPalette.SurfaceContainerHighLight,
        surfaceContainerHighest = FitnessPalette.SurfaceContainerHighestLight,
        surfaceDim = FitnessPalette.SurfaceDimLight,
        surfaceBright = FitnessPalette.Paper98,
        inverseSurface = FitnessPalette.InverseSurfaceLight,
        inverseOnSurface = FitnessPalette.InverseOnSurfaceLight,
        inversePrimary = FitnessPalette.Ember80,
        outline = FitnessPalette.OutlineLight,
        outlineVariant = FitnessPalette.OutlineVariantLight,
        scrim = Color.Black,
    )

    /** Dark scheme — forged charcoal surfaces, glowing ember. */
    fun fitnessDarkColorScheme(): ColorScheme = darkColorScheme(
        primary = FitnessPalette.Ember80,
        onPrimary = FitnessPalette.Ember10,
        primaryContainer = FitnessPalette.Ember30,
        onPrimaryContainer = FitnessPalette.Ember90,
        secondary = FitnessPalette.Steel80,
        onSecondary = FitnessPalette.Steel20,
        secondaryContainer = FitnessPalette.Steel30,
        onSecondaryContainer = FitnessPalette.Steel90,
        tertiary = FitnessPalette.Brass80,
        onTertiary = FitnessPalette.Brass10,
        tertiaryContainer = FitnessPalette.Brass30,
        onTertiaryContainer = FitnessPalette.Brass90,
        error = FitnessPalette.ErrorDark,
        onError = FitnessPalette.OnErrorDark,
        errorContainer = FitnessPalette.Error,
        onErrorContainer = FitnessPalette.ErrorContainer,
        background = FitnessPalette.BackgroundDark,
        onBackground = FitnessPalette.OnBackgroundDark,
        surface = FitnessPalette.BackgroundDark,
        onSurface = FitnessPalette.OnBackgroundDark,
        surfaceVariant = FitnessPalette.SurfaceVariantDark,
        onSurfaceVariant = FitnessPalette.OnSurfaceVariantDark,
        surfaceContainerLowest = FitnessPalette.SurfaceContainerLowestDark,
        surfaceContainerLow = FitnessPalette.SurfaceContainerLowDark,
        surfaceContainer = FitnessPalette.SurfaceContainerDark,
        surfaceContainerHigh = FitnessPalette.SurfaceContainerHighDark,
        surfaceContainerHighest = FitnessPalette.SurfaceContainerHighestDark,
        surfaceDim = FitnessPalette.BackgroundDark,
        surfaceBright = FitnessPalette.SurfaceBrightDark,
        inverseSurface = FitnessPalette.InverseOnSurfaceLight,
        inverseOnSurface = FitnessPalette.InverseSurfaceLight,
        inversePrimary = FitnessPalette.Ember40,
        outline = FitnessPalette.OutlineDark,
        outlineVariant = FitnessPalette.SurfaceVariantDark,
        scrim = Color.Black,
    )

    /** Readiness semantics — mapped to error/warning/success containers. */
    object ReadySemantic {
        val High = FitnessPalette.Success
        val HighContainer = FitnessPalette.SuccessContainer
        val Medium = FitnessPalette.Warning
        val MediumContainer = FitnessPalette.WarningContainer
        val Low = FitnessPalette.Error
        val LowContainer = FitnessPalette.ErrorContainer
    }

    // ------------------------------------------------------------------
    // md.sys.shape corner scale — none → full (typography-and-shape.md).
    // ------------------------------------------------------------------
    object FitnessShapeScale {
        val None: Dp = 0.dp
        val ExtraSmall: Dp = 4.dp
        val Small: Dp = 8.dp
        val Medium: Dp = 12.dp
        val Large: Dp = 16.dp
        val LargeIncreased: Dp = 20.dp
        val ExtraLarge: Dp = 28.dp
        val ExtraLargeIncreased: Dp = 32.dp
        val ExtraExtraLarge: Dp = 48.dp
        val Full: Shape = RoundedCornerShape(percent = 50)
    }

    /** Default [Shapes] handed to MaterialTheme — the md.sys baseline corners. */
    val materialShapes: Shapes = Shapes(
        extraSmall = RoundedCornerShape(FitnessShapeScale.ExtraSmall),
        small = RoundedCornerShape(FitnessShapeScale.Small),
        medium = RoundedCornerShape(FitnessShapeScale.Medium),
        large = RoundedCornerShape(FitnessShapeScale.Large),
        extraLarge = RoundedCornerShape(FitnessShapeScale.ExtraLarge),
    )

    // ------------------------------------------------------------------
    // Fitness shape ROLES — not one radius everywhere; contrast is expressive.
    // Asymmetric customs stay literal-dp by design (documented deviations).
    // ------------------------------------------------------------------
    object FitnessShapes {
        /** Hero — organic large container (Today hero, Progress 1RM). */
        val hero: Shape = RoundedCornerShape(
            topStart = 56.dp, topEnd = FitnessShapeScale.Medium,
            bottomStart = FitnessShapeScale.Large, bottomEnd = 44.dp,
        )
        val heroInverse: Shape = RoundedCornerShape(
            topStart = FitnessShapeScale.Medium, topEnd = 56.dp,
            bottomStart = 44.dp, bottomEnd = FitnessShapeScale.Large,
        )
        val heroSquircle: Shape = RoundedCornerShape(FitnessShapeScale.ExtraExtraLarge)

        /** CompactAction — near-circle primary action (START button, chips). */
        val compactAction: Shape = RoundedCornerShape(FitnessShapeScale.ExtraLarge)
        val compactActionCircle: Shape = CircleShape
        val compactActionSquircle: Shape = RoundedCornerShape(36.dp)
        val pill: Shape = FitnessShapeScale.Full

        /** Achievement — soft-square metric/achievement containers. */
        val achievement: Shape = RoundedCornerShape(24.dp)
        val achievementSmall: Shape = RoundedCornerShape(18.dp)
        val achievementPill: Shape = RoundedCornerShape(100.dp)

        /** Media — asymmetrical artwork frames. */
        val media: Shape = RoundedCornerShape(
            topStart = 36.dp, topEnd = FitnessShapeScale.Medium,
            bottomEnd = 32.dp, bottomStart = FitnessShapeScale.Medium,
        )
        val mediaAlt: Shape = RoundedCornerShape(
            topStart = FitnessShapeScale.Medium, topEnd = 36.dp,
            bottomStart = 32.dp, bottomEnd = FitnessShapeScale.Medium,
        )

        /** Sheet — XL top corners, square bottom. */
        val sheet: Shape = RoundedCornerShape(
            topStart = 36.dp, topEnd = 36.dp,
            bottomStart = FitnessShapeScale.None, bottomEnd = FitnessShapeScale.None,
        )
        val sheetLarge: Shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp)

        /** Nav — floating pill navigation. */
        val navPill: Shape = RoundedCornerShape(FitnessShapeScale.ExtraLargeIncreased)
        val navBar: Shape = RoundedCornerShape(FitnessShapeScale.ExtraLarge)

        /** Timer — organic timer container + pulse variant. */
        val timer: Shape = RoundedCornerShape(40.dp)
        val timerPulse: Shape = RoundedCornerShape(44.dp)

        /** Cards — large/medium/small tiers. */
        val cardLarge: Shape = RoundedCornerShape(FitnessShapeScale.ExtraLargeIncreased)
        val cardMedium: Shape = RoundedCornerShape(24.dp)
        val cardSmall: Shape = RoundedCornerShape(FitnessShapeScale.LargeIncreased)
    }

    // ------------------------------------------------------------------
    // 8dp spacing system (layout-and-responsive.md — I/O 2026 guidance).
    // ------------------------------------------------------------------
    object FitnessSpacing {
        val xxs: Dp = 4.dp
        val xs: Dp = 8.dp
        val sm: Dp = 12.dp
        val md: Dp = 16.dp
        val lg: Dp = 24.dp
        val xl: Dp = 32.dp
        val xxl: Dp = 48.dp
    }
}
