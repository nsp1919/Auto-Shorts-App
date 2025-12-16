package com.autoshorts.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Dark color scheme for Auto Shorts app.
 * Optimized for creator-style UI with vibrant accents.
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = PrimaryBlueDark,
    onPrimaryContainer = PrimaryBlueLight,

    // Secondary colors
    secondary = SecondaryPurple,
    onSecondary = Color.White,
    secondaryContainer = SecondaryPurpleDark,
    onSecondaryContainer = SecondaryPurpleLight,

    // Tertiary colors (Accent)
    tertiary = AccentPink,
    onTertiary = Color.White,
    tertiaryContainer = AccentPinkDark,
    onTertiaryContainer = AccentPinkLight,

    // Background
    background = BackgroundDark,
    onBackground = TextPrimary,

    // Surface
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDarkElevated,
    onSurfaceVariant = TextSecondary,

    // Outline
    outline = BorderDark,
    outlineVariant = BorderLight,

    // Error
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedDark,
    onErrorContainer = Color.White,

    // Inverse
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundDark,
    inversePrimary = PrimaryBlueDark
)

/**
 * Auto Shorts theme wrapper.
 * Forces dark mode for consistent creator-style UI.
 */
@Composable
fun AutoShortsTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar color to match background
            window.statusBarColor = BackgroundDark.toArgb()
            window.navigationBarColor = BackgroundDark.toArgb()
            // Use light icons on dark background
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AutoShortsTypography,
        content = content
    )
}
