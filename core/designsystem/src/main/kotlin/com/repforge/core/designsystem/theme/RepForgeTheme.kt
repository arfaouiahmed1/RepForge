package com.repforge.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.repforge.core.designsystem.token.FitnessVisualTokens
import com.repforge.core.designsystem.token.RepForgeTypography

/**
 * RepForge theme — wires the single token source ([FitnessVisualTokens]) into
 * MaterialTheme: full md.sys color roles (light/dark) and the md.sys corner scale.
 *
 * Motion: M3's MotionScheme API is internal in material3 1.4.0 (expressive line not
 * yet public), so component motion comes from the spring vocabulary in
 * [com.repforge.core.designsystem.token.RepForgeMotion] — emphasized/standard/quick
 * springs per M3 Expressive. Adopt MotionScheme.expressive() when M3 exposes it.
 *
 * Dynamic color follows theming-and-dynamic-color.md guidance: wallpaper-derived
 * schemes on Android 12+ when enabled. Default is OFF — RepForge leads with its own
 * expressive ember/forge/paper palette; the fallback path stays available for A/B.
 */
@Composable
fun RepForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> FitnessVisualTokens.fitnessDarkColorScheme()
        else -> FitnessVisualTokens.fitnessLightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RepForgeTypography,
        shapes = FitnessVisualTokens.materialShapes,
        content = content
    )
}

// Expressive shape ROLES (hero/media/compactAction/achievement/timer/navPill) are
// accessed via RepForgeShapes / FitnessVisualTokens.FitnessShapes — asymmetric
// morph targets that MaterialTheme.shapes' five slots cannot express.
