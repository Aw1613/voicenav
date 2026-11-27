package com.elderassist.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Warm, high-contrast editorial color tokens designed for elder accessibility.
 */
object ElderAssistColors {
    val Surface = Color(0xFFFDF9F3)
    val SurfaceContainerLow = Color(0xFFF7F3ED)
    val SurfaceContainer = Color(0xFFF1EDE7)
    val SurfaceContainerHigh = Color(0xFFEBE8E2)
    val SurfaceContainerHighest = Color(0xFFE6E2DC)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)

    val Primary = Color(0xFF152434)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryFixed = Color(0xFFD4E4F9)
    val OnPrimaryFixed = Color(0xFF0D1D2C)

    val Secondary = Color(0xFF9D4225)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFD8C68)
    val SecondaryFixed = Color(0xFFFFDBD0)
    val OnSecondaryFixed = Color(0xFF3A0B00)
    val OnSecondaryFixedVariant = Color(0xFF7E2C10)

    val TertiaryFixed = Color(0xFFFFDAD2)
    val OnTertiaryFixedVariant = Color(0xFF822714)

    val OnSurface = Color(0xFF1C1C18)
    val OnSurfaceVariant = Color(0xFF44474C)
    val Outline = Color(0xFF74777C)
    val OutlineVariant = Color(0xFFC4C6CC)

    val Error = Color(0xFFBA1A1A)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    val Success = Color(0xFF136F43)
    val SuccessContainer = Color(0xFFECF8F2)
    val Warning = Color(0xFF9A5800)
    val WarningContainer = Color(0xFFFFF8EC)
}

private val ElderAssistLightColorScheme = lightColorScheme(
    primary = ElderAssistColors.Primary,
    onPrimary = ElderAssistColors.OnPrimary,
    primaryContainer = ElderAssistColors.PrimaryFixed,
    onPrimaryContainer = ElderAssistColors.OnPrimaryFixed,
    secondary = ElderAssistColors.Secondary,
    onSecondary = ElderAssistColors.OnSecondary,
    secondaryContainer = ElderAssistColors.SecondaryFixed,
    onSecondaryContainer = ElderAssistColors.OnSecondaryFixed,
    tertiaryContainer = ElderAssistColors.TertiaryFixed,
    onTertiaryContainer = ElderAssistColors.OnTertiaryFixedVariant,
    background = ElderAssistColors.Surface,
    onBackground = ElderAssistColors.OnSurface,
    surface = ElderAssistColors.Surface,
    onSurface = ElderAssistColors.OnSurface,
    surfaceVariant = ElderAssistColors.SurfaceContainerHighest,
    onSurfaceVariant = ElderAssistColors.OnSurfaceVariant,
    outline = ElderAssistColors.Outline,
    outlineVariant = ElderAssistColors.OutlineVariant,
    error = ElderAssistColors.Error,
    onError = Color.White,
    errorContainer = ElderAssistColors.ErrorContainer,
    onErrorContainer = ElderAssistColors.OnErrorContainer
)

private val ElderAssistTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    )
)

/**
 * Applies the ElderAssist warm editorial Material 3 theme.
 */
@Composable
fun ElderAssistTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ElderAssistLightColorScheme,
        typography = ElderAssistTypography,
        content = content
    )
}
