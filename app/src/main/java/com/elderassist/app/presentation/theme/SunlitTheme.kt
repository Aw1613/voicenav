package com.elderassist.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.sp
import com.elderassist.app.settings.AppTextScale
import com.elderassist.app.settings.AppThemeMode

object SunlitColors {
    val Surface = Color(0xFFFCF9F4)
    val SurfaceDim = Color(0xFFDCDAD5)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerLow = Color(0xFFF6F3EE)
    val SurfaceContainer = Color(0xFFF0EDE9)
    val SurfaceContainerHigh = Color(0xFFEBE8E3)
    val SurfaceContainerHighest = Color(0xFFE5E2DD)
    val OnSurface = Color(0xFF1C1C19)
    val OnSurfaceVariant = Color(0xFF424752)
    val Outline = Color(0xFF727783)
    val OutlineVariant = Color(0xFFC2C6D4)

    val Primary = Color(0xFF005BB2)
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF2F74CE)
    val PrimaryFixed = Color(0xFFD6E3FF)
    val OnPrimaryFixed = Color(0xFF001B3D)
    val OnPrimaryFixedVariant = Color(0xFF00468C)

    val Secondary = Color(0xFF99462A)
    val OnSecondary = Color(0xFFFFFFFF)
    val SecondaryContainer = Color(0xFFFE9572)
    val OnSecondaryContainer = Color(0xFF762C12)
    val SecondaryFixed = Color(0xFFFFDBD0)
    val OnSecondaryFixed = Color(0xFF390B00)

    val Tertiary = Color(0xFF8A4D00)
    val OnTertiary = Color(0xFFFFFFFF)
    val TertiaryFixed = Color(0xFFFFDCC0)
    val OnTertiaryFixed = Color(0xFF2D1600)
    val OnTertiaryFixedVariant = Color(0xFF6B3B00)

    val Error = Color(0xFFBA1A1A)
    val OnError = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)

    val AmbientGlowStart = Color(0xFFFED7AA)
    val AmbientGlowMid = Color(0xFFFDBA74)
    val SuccessGreen = Color(0xFF136F43)
    val SuccessBg = Color(0xFFECF8F2)
}

private val LightColors = lightColorScheme(
    primary = SunlitColors.Primary,
    onPrimary = SunlitColors.OnPrimary,
    primaryContainer = SunlitColors.PrimaryFixed,
    onPrimaryContainer = SunlitColors.OnPrimaryFixed,
    secondary = SunlitColors.Secondary,
    onSecondary = SunlitColors.OnSecondary,
    secondaryContainer = SunlitColors.SecondaryFixed,
    onSecondaryContainer = SunlitColors.OnSecondaryFixed,
    tertiary = SunlitColors.Tertiary,
    onTertiary = SunlitColors.OnTertiary,
    tertiaryContainer = SunlitColors.TertiaryFixed,
    onTertiaryContainer = SunlitColors.OnTertiaryFixed,
    error = SunlitColors.Error,
    onError = SunlitColors.OnError,
    errorContainer = SunlitColors.ErrorContainer,
    onErrorContainer = SunlitColors.OnErrorContainer,
    background = SunlitColors.Surface,
    onBackground = SunlitColors.OnSurface,
    surface = SunlitColors.Surface,
    onSurface = SunlitColors.OnSurface,
    surfaceVariant = SunlitColors.SurfaceContainerLow,
    onSurfaceVariant = SunlitColors.OnSurfaceVariant,
    surfaceContainerLowest = SunlitColors.SurfaceContainerLowest,
    surfaceContainerLow = SunlitColors.SurfaceContainerLow,
    surfaceContainer = SunlitColors.SurfaceContainer,
    surfaceContainerHigh = SunlitColors.SurfaceContainerHigh,
    surfaceContainerHighest = SunlitColors.SurfaceContainerHighest,
    outline = SunlitColors.Outline,
    outlineVariant = SunlitColors.OutlineVariant
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA9C7FF),
    onPrimary = Color(0xFF003063),
    primaryContainer = Color(0xFF00468C),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFFFB59E),
    onSecondary = Color(0xFF5A1C07),
    secondaryContainer = Color(0xFF762C12),
    onSecondaryContainer = Color(0xFFFFDBD0),
    tertiary = Color(0xFFFFB876),
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = Color(0xFF6B3B00),
    onTertiaryContainer = Color(0xFFFFDCC0),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF171614),
    onBackground = Color(0xFFF3F0EB),
    surface = Color(0xFF171614),
    onSurface = Color(0xFFF3F0EB),
    surfaceVariant = Color(0xFF282623),
    onSurfaceVariant = Color(0xFFC8C5BF),
    surfaceContainerLowest = Color(0xFF22201D),
    surfaceContainerLow = Color(0xFF282623),
    surfaceContainer = Color(0xFF2E2C29),
    surfaceContainerHigh = Color(0xFF383531),
    surfaceContainerHighest = Color(0xFF423F3A),
    outline = Color(0xFF918E88),
    outlineVariant = Color(0xFF494640)
)

private val SunlitTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.015).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 30.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun ElderAssistTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    textScale: AppTextScale = AppTextScale.DEFAULT,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val currentDensity = LocalDensity.current
    val scaledDensity = Density(
        density = currentDensity.density,
        fontScale = (currentDensity.fontScale * textScale.multiplier).coerceAtMost(2.2f)
    )

    CompositionLocalProvider(LocalDensity provides scaledDensity) {
        MaterialTheme(
            colorScheme = if (isDark) DarkColors else LightColors,
            typography = SunlitTypography,
            content = content
        )
    }
}
