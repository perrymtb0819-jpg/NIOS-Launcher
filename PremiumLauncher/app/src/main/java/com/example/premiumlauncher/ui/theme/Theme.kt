package com.example.premiumlauncher.ui.theme

import androidx.compose.foundation.IsSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = 0xFF476AFF.toArgb(),
    secondary = 0xFF625DEA.toArgb(),
    tertiary = 0xFF645BFF.toArgb(),
    background = 0xFFFFFFFF.toArgb(),
    surface = 0xFFFFFFFF.toArgb(),
    error = 0xFFB00020.toArgb(),
    onPrimary = 0xFFFFFFFF.toArgb(),
    onSecondary = 0xFFFFFFFF.toArgb(),
    onTertiary = 0xFFFFFFFF.toArgb(),
    onBackground = 0xFF1D1D1D.toArgb(),
    onSurface = 0xFF1D1D1D.toArgb(),
    onError = 0xFFFFFFFF.toArgb()
)

private val DarkColors = darkColorScheme(
    primary = 0xFF7C8DFF.toArgb(),
    secondary = 0xFF918AFF.toArgb(),
    tertiary = 0xFFC5B4FF.toArgb(),
    background = 0xFF121212.toArgb(),
    surface = 0xFF121212.toArgb(),
    error = 0xFFCF6679.toArgb(),
    onPrimary = 0xFF000000.toArgb(),
    onSecondary = 0xFF000000.toArgb(),
    onTertiary = 0xFF000000.toArgb(),
    onBackground = 0xFFE6E1E5.toArgb(),
    onSurface = 0xFFE6E1E5.toArgb(),
    onError = 0xFF000000.toArgb()
)

// Settings theme variations
private val SettingsLightColors = lightColorScheme(
    primary = 0xFF476AFF.toArgb(),
    secondary = 0xFF625DEA.toArgb(),
    tertiary = 0xFF645BFF.toArgb(),
    background = 0xFF121212.toArgb(), // Dark background for settings
    surface = 0xFF1E1E1E.toArgb(),
    error = 0xFFB00020.toArgb(),
    onPrimary = 0xFFFFFFFF.toArgb(),
    onSecondary = 0xFFFFFFFF.toArgb(),
    onTertiary = 0xFFFFFFFF.toArgb(),
    onBackground = 0xFFE6E1E5.toArgb(),
    onSurface = 0xFFE6E1E5.toArgb(),
    onError = 0xFFFFFFFF.toArgb()
)

private val SettingsDarkColors = darkColorScheme(
    primary = 0xFF7C8DFF.toArgb(),
    secondary = 0xFF918AFF.toArgb(),
    tertiary = 0xFFC5B4FF.toArgb(),
    background = 0xFF121212.toArgb(),
    surface = 0xFF1E1E1E.toArgb(),
    error = 0xFFCF6679.toArgb(),
    onPrimary = 0xFFFFFFFF.toArgb(),
    onSecondary = 0xFFFFFFFF.toArgb(),
    onTertiary = 0xFFFFFFFF.toArgb(),
    onBackground = 0xFFE6E1E5.toArgb(),
    onSurface = 0xFFE6E1E5.toArgb(),
    onError = 0xFF000000.toArgb()
)

@Composable
fun PremiumLauncherTheme(
    darkTheme: Boolean = IsSystemInDarkTheme(),
    // Dynamic color is available on Android 12+ only.
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        dynamicTheme && !darkTheme -> dynamicLightColorScheme(LocalContext.current)
        darkTheme -> DarkColors
        else -> LightColors
    }
    val typography = Typography(
        bodyLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
        titleLarge = TextStyle(fontWeight = FontStyle.Normal, fontSize = 22.sp),
        titleMedium = TextStyle(fontWeight = FontStyle.Normal, fontSize = 18.sp),
        titleSmall = TextStyle(fontWeight = FontStyle.Normal, fontSize = 16.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp)
    )
    Material3Theme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun SettingsTheme(
    darkTheme: Boolean = IsSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> SettingsDarkColors
        else -> SettingsLightColors
    }
    val typography = Typography(
        bodyLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
        titleLarge = TextStyle(fontWeight = FontStyle.Normal, fontSize = 22.sp),
        titleMedium = TextStyle(fontWeight = FontStyle.Normal, fontSize = 18.sp),
        titleSmall = TextStyle(fontWeight = FontStyle.Normal, fontSize = 16.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp)
    )
    Material3Theme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}