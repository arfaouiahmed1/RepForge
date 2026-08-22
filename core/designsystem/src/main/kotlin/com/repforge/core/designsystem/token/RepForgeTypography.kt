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
 * On Android 8/O+ use FontVariation.Settings. See docs for fallback if downloadable fonts lag.
 */
private val GoogleSansFlex = FontFamily(
    Font(R.font.google_sans_flex, FontWeight.W400),
    Font(R.font.google_sans_flex, FontWeight.W500),
    Font(R.font.google_sans_flex, FontWeight.W600),
    Font(R.font.google_sans_flex, FontWeight.W700),
    Font(R.font.google_sans_flex, FontWeight.W800),
    Font(R.font.google_sans_flex, FontWeight.W900),
)
private val GoogleSansFlexFallback = GoogleSansFlex

fun flexVariation(
    weight: Int = 400,
    width: Float = 100f,
    opticalSize: Float = 14f,
    grade: Int? = null,
    roundedness: Int? = null
): FontVariation.Settings {
    val base = mutableListOf(
        FontVariation.weight(weight),
        FontVariation.width(width),
        FontVariation.Setting("opsz", opticalSize),
    )
    if (grade != null) base.add(FontVariation.Setting("GRAD", grade.toFloat()))
    if (roundedness != null) base.add(FontVariation.Setting("ROND", roundedness.toFloat()))
    return FontVariation.Settings(*base.toTypedArray())
}

object RepForgeTypeRoles {
    val DisplayHero = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W800,
        fontSize = 62.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1.8).sp,
        fontVariationSettings = flexVariation(weight = 800, width = 75f, opticalSize = 72f, grade = 0, roundedness = 0)
    )
    val DisplayHeroLarge = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W800,
        fontSize = 84.sp,
        lineHeight = 72.sp,
        letterSpacing = (-2.5).sp,
        fontVariationSettings = flexVariation(weight = 900, width = 70f, opticalSize = 84f, grade = 0, roundedness = 0)
    )

    val HeadlineLoud = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W750,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.6).sp,
        fontVariationSettings = flexVariation(weight = 750, width = 80f, opticalSize = 28f, grade = 0, roundedness = 12)
    )

    val MetricLarge = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W700,
        fontSize = 52.sp,
        lineHeight = 52.sp,
        letterSpacing = (-1.2).sp,
        fontVariationSettings = flexVariation(weight = 700, width = 85f, opticalSize = 48f, grade = 0, roundedness = 18)
    )
    val MetricMedium = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W650,
        fontSize = 34.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp,
        fontVariationSettings = flexVariation(weight = 650, width = 90f, opticalSize = 32f, grade = 0, roundedness = 14)
    )

    val BodySupport = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp,
        fontVariationSettings = flexVariation(weight = 400, width = 100f, opticalSize = 14f, grade = 0, roundedness = 0)
    )
    val LabelExpressive = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W700,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.6.sp,
        fontVariationSettings = flexVariation(weight = 700, width = 85f, opticalSize = 11f, grade = 0, roundedness = 0)
    )
    val LabelSmall = TextStyle(
        fontFamily = GoogleSansFlexFallback,
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.8.sp,
        fontVariationSettings = flexVariation(weight = 600, width = 90f, opticalSize = 10f, grade = 0, roundedness = 0)
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
