package com.repforge.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.repforge.core.designsystem.token.RepForgeColors
import com.repforge.core.designsystem.token.RepForgeShapes
import com.repforge.core.designsystem.token.RepForgeTypography

private val LightColorScheme = lightColorScheme(
    primary = RepForgeColors.Ember35,
    onPrimary = Color.White,
    primaryContainer = RepForgeColors.Ember90,
    onPrimaryContainer = RepForgeColors.Ember10,
    secondary = RepForgeColors.Steel40,
    onSecondary = Color.White,
    secondaryContainer = RepForgeColors.Steel90,
    onSecondaryContainer = Color(0xFF001E30),
    tertiary = RepForgeColors.Brass40,
    onTertiary = Color.White,
    tertiaryContainer = RepForgeColors.Brass90,
    onTertiaryContainer = Color(0xFF211B00),
    background = RepForgeColors.Paper98,
    onBackground = RepForgeColors.Forge10,
    surface = RepForgeColors.Paper98,
    onSurface = RepForgeColors.Forge10,
    surfaceVariant = RepForgeColors.Paper90,
    onSurfaceVariant = RepForgeColors.Forge20,
    surfaceContainer = Color(0xFFF8EEE8),
    surfaceContainerHigh = Color(0xFFF2E6DF),
    surfaceContainerHighest = Color(0xFFEBDDD5),
    error = RepForgeColors.Error,
    onError = Color.White,
    errorContainer = RepForgeColors.ErrorContainer,
    onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF85736E),
    outlineVariant = Color(0xFFD7C2BC),
    scrim = Color(0xFF000000),
)

private val DarkColorScheme = darkColorScheme(
    primary = RepForgeColors.Ember80,
    onPrimary = RepForgeColors.Ember10,
    primaryContainer = RepForgeColors.Ember35,
    onPrimaryContainer = RepForgeColors.Ember90,
    secondary = RepForgeColors.Steel80,
    onSecondary = Color(0xFF00344F),
    secondaryContainer = RepForgeColors.Steel30,
    onSecondaryContainer = RepForgeColors.Steel90,
    tertiary = RepForgeColors.Brass80,
    onTertiary = Color(0xFF3A3000),
    tertiaryContainer = Color(0xFF534600),
    onTertiaryContainer = RepForgeColors.Brass90,
    background = Color(0xFF201A18),
    onBackground = Color(0xFFEDE0DB),
    surface = Color(0xFF201A18),
    onSurface = Color(0xFFEDE0DB),
    surfaceVariant = Color(0xFF53433F),
    onSurfaceVariant = Color(0xFFD7C2BC),
    surfaceContainer = Color(0xFF271E1C),
    surfaceContainerHigh = Color(0xFF322823),
    surfaceContainerHighest = Color(0xFF3D2F2B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = RepForgeColors.ErrorContainer,
    outline = Color(0xFFA08C87),
    outlineVariant = Color(0xFF53433F),
)

@Composable
fun RepForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // RepForge uses its own expressive palette, not dynamic
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Isolate experimental MotionScheme inside designsystem — do not leak to features
    // val motionScheme = MotionScheme.expressive() // use when M3 1.5 stable is adopted

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RepForgeTypography,
        // shapes are provided via RepForgeShapes object — not global ShapeTokens, to enforce role-based usage
        content = content
    )
}

// Shape roles are accessed via RepForgeShapes.Hero etc — not via MaterialTheme.shapes
// This enforces: Hero, PrimaryAction, Metric, Media, Sheet, NavPill, Timer
