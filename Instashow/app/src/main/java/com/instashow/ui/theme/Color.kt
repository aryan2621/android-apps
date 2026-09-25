package com.instashow.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF0B0B0D)
val Surface = Color(0xFF151518)
val SurfaceHigh = Color(0xFF1F1F24)
val Line = Color(0xFF2B2B32)
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFF9B9BA4)
val TextMuted = Color(0xFF63636C)
val Lime = Color(0xFFD7FF3A)
val LimeMuted = Color(0xFF4A5A12)
val Ember = Color(0xFFFF6B3D)
val Danger = Color(0xFFFF5A5F)

/** Instagram's own gradient, used only on the button that opens Instagram. */
val InstagramGradient = Brush.linearGradient(
    listOf(Color(0xFFFEDA75), Color(0xFFFA7E1E), Color(0xFFD62976), Color(0xFF962FBF), Color(0xFF4F5BD5)),
)
