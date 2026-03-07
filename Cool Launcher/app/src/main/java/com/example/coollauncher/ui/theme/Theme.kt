package com.example.coollauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** Accent colors for the selected color theme (Teal, Orange, Red, etc.). */
data class AccentColors(
    val primary: Color,
    val onPrimary: Color,
    val glow: Color,
)

/** 0=Teal, 1=Orange, 2=Red, 3=Green, 4=Yellow, 5=Blue, 6=Purple */
fun accentColorsFor(key: Int): AccentColors = when (key) {
    1 -> AccentColors(
        primary = Color(0xFFFF9800),
        onPrimary = Color(0xFF1A0A00),
        glow = Color(0x40FF9800),
    )
    2 -> AccentColors(
        primary = Color(0xFFE53935),
        onPrimary = Color(0xFFFFFFFF),
        glow = Color(0x40E53935),
    )
    3 -> AccentColors(
        primary = Color(0xFF43A047),
        onPrimary = Color(0xFF0A1A0B),
        glow = Color(0x4043A047),
    )
    4 -> AccentColors(
        primary = Color(0xFFFDD835),
        onPrimary = Color(0xFF1A1800),
        glow = Color(0x40FDD835),
    )
    5 -> AccentColors(
        primary = Color(0xFF1E88E5),
        onPrimary = Color(0xFFFFFFFF),
        glow = Color(0x401E88E5),
    )
    6 -> AccentColors(
        primary = Color(0xFF8E24AA),
        onPrimary = Color(0xFFFFFFFF),
        glow = Color(0x408E24AA),
    )
    else -> AccentColors(
        primary = accentPrimary,
        onPrimary = onAccent,
        glow = accentGlow,
    )
}

val LocalAccentColors = compositionLocalOf<AccentColors> {
    AccentColors(primary = accentPrimary, onPrimary = onAccent, glow = accentGlow)
}

val LocalFontFamily = compositionLocalOf<FontFamily> { FontFamily.Default }

/** When true, show app names only (no icons) everywhere. */
val LocalHardMinimalMode = compositionLocalOf { false }

/** 0=default, 1=serif, 2=monospace */
fun fontFamilyFor(key: Int): FontFamily = when (key) {
    1 -> FontFamily.Serif
    2 -> FontFamily.Monospace
    else -> FontFamily.Default
}

@Composable
fun CoolLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontKey: Int = 0,
    colorThemeKey: Int = 0,
    hardMinimalMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val accent = accentColorsFor(colorThemeKey)
    val fontFamily = fontFamilyFor(fontKey)

    val darkColorScheme = darkColorScheme(
        primary = accent.primary,
        onPrimary = accent.onPrimary,
        primaryContainer = surfaceCard,
        onPrimaryContainer = textPrimary,
        background = backgroundDark,
        onBackground = textPrimary,
        surface = surfaceCard,
        onSurface = textPrimary,
        surfaceVariant = surfaceElevated,
        onSurfaceVariant = textSecondary,
        outline = borderSubtle,
    )
    val lightColorScheme = lightColorScheme(
        primary = accent.primary,
        onPrimary = accent.onPrimary,
        primaryContainer = surfaceCard,
        onPrimaryContainer = textPrimary,
        background = Color(0xFFF5F5F7),
        onBackground = Color(0xFF0D0D0F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0D0D0F),
        surfaceVariant = surfaceElevated,
        onSurfaceVariant = textSecondary,
        outline = borderSubtle,
    )
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            CompositionLocalProvider(
                LocalAccentColors provides accent,
                LocalFontFamily provides fontFamily,
                LocalHardMinimalMode provides hardMinimalMode,
            ) {
                content()
            }
        }
    )
}