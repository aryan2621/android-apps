package com.pause

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Night = Color(0xFF0E1116)
val Surface = Color(0xFF171B22)
val SurfaceHigh = Color(0xFF222833)
val Line = Color(0xFF262C36)
val Mint = Color(0xFF8FE3C6)
val TextPrimary = Color(0xFFEEF2F6)
val TextSecondary = Color(0xFF98A2B3)

@Composable
fun PauseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Mint,
            onPrimary = Night,
            background = Night,
            onBackground = TextPrimary,
            surface = Surface,
            onSurface = TextPrimary,
            surfaceVariant = SurfaceHigh,
            onSurfaceVariant = TextSecondary,
            outline = Line,
        ),
        content = content,
    )
}
