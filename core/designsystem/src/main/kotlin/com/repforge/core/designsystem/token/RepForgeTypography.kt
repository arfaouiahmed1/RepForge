@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.repforge.core.designsystem.token

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.repforge.core.designsystem.R

/**
 * Google Sans Flex as variable font — 6 axes: weight, width, optical size, slant, grade, roundedness.
 * Bundle the variable TTF under res/font/google_sans_flex.ttf and provide fallback.
 * Only animate axes to establish hierarchy, not constantly.
 *
 * Variable-font axes are attached to [Font] instances via `variationSettings`
 * (TextStyle has no font-variation parameter). Each type role therefore gets its
 * own single-instance FontFamily carrying its exact axis configuration.
 */

/** Builds Google Sans Flex variation settings for the 6 supported axes. */
fun flexVariation(
    weight: Int = 400,
    width: Float = 100f,
    opticalSize: Float = 14f,
    grade: Int? = null,
    roundedness: Int? = null,
): FontVariation.Settings {
    val settings = mutableListOf(
        FontVariation.weight(weight),
        FontVariation.width(width),
        FontVariation.Setting("opsz", opticalSize),
    )
    if (grade != null) settings.add(FontVariation.Setting("GRAD", grade.toFloat()))
    if (roundedness != null) settings.add(FontVariation.Setting("ROND", roundedness.toFloat()))
    return FontVariation.Settings(*settings.toTypedArray())
}

/** Single-instance variable FontFamily: one TTF, exact axis configuration. */
private fun gsFlex(
    weight: FontWeight,
    variation: FontVariation.Settings,
): FontFamily = FontFamily(
    Font(R.font.google_sans_flex, weight = weight, variationSettings = variation),
)

object RepForgeTypeRoles {
    val DisplayHero = TextStyle(
        fontFamily = gsFlex(FontWeight.W800, flexVariation(weight = 800, width = 75f, opticalSize = 72f, grade = 0, roundedness = 0)),
        fontWeight = FontWeight.W800,
        fontSize = 62.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1.8).sp,
    )
    val DisplayHeroLarge = TextStyle(
        fontFamily = gsFlex(FontWeight.W900, flexVariation(weight = 900, width = 70f, opticalSize = 84f, grade = 0, roundedness = 0)),
        fontWeight = FontWeight.W900,
        fontSize = 84.sp,
        lineHeight = 72.sp,
        letterSpacing = (-2.5).sp,
    )

    val HeadlineLoud = TextStyle(
        fontFamily = gsFlex(FontWeight(750), flexVariation(weight = 750, width = 80f, opticalSize = 28f, grade = 0, roundedness = 12)),
        fontWeight = FontWeight(750),
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.6).sp,
    )

    val MetricLarge = TextStyle(
        fontFamily = gsFlex(FontWeight.W700, flexVariation(weight = 700, width = 85f, opticalSize = 48f, grade = 0, roundedness = 18)),
        fontWeight = FontWeight.W700,
        fontSize = 52.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.2).sp,
    )
    val MetricMedium = TextStyle(
        fontFamily = gsFlex(FontWeight(650), flexVariation(weight = 650, width = 90f, opticalSize = 32f, grade = 0, roundedness = 14)),
        fontWeight = FontWeight(650),
        fontSize = 34.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp,
    )

    val BodySupport = TextStyle(
        fontFamily = gsFlex(FontWeight.W400, flexVariation(weight = 400, width = 100f, opticalSize = 14f)),
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
    )
    val LabelExpressive = TextStyle(
        fontFamily = gsFlex(FontWeight.W700, flexVariation(weight = 700, width = 85f, opticalSize = 11f)),
        fontWeight = FontWeight.W700,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
    )
    val LabelSmall = TextStyle(
        fontFamily = gsFlex(FontWeight.W600, flexVariation(weight = 600, width = 90f, opticalSize = 10f)),
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
    )
}

val RepForgeTypography = Typography(
    displayLarge = RepForgeTypeRoles.DisplayHeroLarge,
    displayMedium = RepForgeTypeRoles.DisplayHero,
    headlineLarge = RepForgeTypeRoles.HeadlineLoud,
    headlineMedium = RepForgeTypeRoles.MetricLarge,
    titleLarge = RepForgeTypeRoles.MetricMedium,
    bodyMedium = RepForgeTypeRoles.BodySupport,
    labelLarge = RepForgeTypeRoles.LabelExpressive,
)
