package com.example.coollauncher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTypography {
    @Composable
    @ReadOnlyComposable
    fun headline(): TextStyle = TextStyle(
        fontFamily = LocalFontFamily.current,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.2).sp,
    )
    @Composable
    @ReadOnlyComposable
    fun sectionTitle(): TextStyle = TextStyle(
        fontFamily = LocalFontFamily.current,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp,
    )
    @Composable
    @ReadOnlyComposable
    fun body(): TextStyle = TextStyle(
        fontFamily = LocalFontFamily.current,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
    )
    @Composable
    @ReadOnlyComposable
    fun bodySmall(): TextStyle = TextStyle(
        fontFamily = LocalFontFamily.current,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    )
    @Composable
    @ReadOnlyComposable
    fun caption(): TextStyle = TextStyle(
        fontFamily = LocalFontFamily.current,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    )
    @Composable
    @ReadOnlyComposable
    fun button(): TextStyle = TextStyle(
        fontFamily = LocalFontFamily.current,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
    )
}
