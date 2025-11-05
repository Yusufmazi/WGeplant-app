package com.wgeplant.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    secondary = md_theme_light_secondary,
    tertiary = md_theme_light_tertiary,
    onPrimary = md_theme_light_onPrimary,
    onSecondary = md_theme_light_onSecondary,
    error = md_theme_light_error,
    onSurfaceVariant = Color.Black,
    surfaceVariant = Color(0xFFDCE3F7),
    surface = Color(0xFFC5D3F0),
    onSurface = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    error = md_theme_light_error,
    secondary = Color(0xFFD0BCFF),
    onSecondary = Color(0xFF000000),
    primary = Color(0xFF665889),
    onPrimary = Color(0xFFFFFFFF),
    tertiary = Color(0xFFB08ABE),
    surface = Color(0xFFE6D9FF),
    onSurface = Color.White
)

/**
 * Composable that applies the WGeplant app theme.
 *
 * @param darkTheme Controls whether to use the dark theme. Defaults to system setting.
 * @param dynamicColor Enables dynamic color schemes (Material You) on supported devices (Android 12+).
 * @param content The content to which the theme will be applied.
 */
@Composable
fun WGeplantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
