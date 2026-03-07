package com.example.coollauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.textMuted
import com.example.coollauncher.ui.theme.textPrimary

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val accentColors = LocalAccentColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 10.dp,
                shape = AppShapes.pillLarge,
                spotColor = accentColors.primary.copy(alpha = 0.4f),
                ambientColor = accentColors.primary.copy(alpha = 0.15f),
            )
            .clip(AppShapes.pillLarge)
            .background(
                if (enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(accentColors.primary, accentColors.primary.copy(alpha = 0.88f)),
                    )
                } else {
                    Brush.horizontalGradient(colors = listOf(textMuted, textMuted))
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = AppTypography.button(),
            color = if (enabled) accentColors.onPrimary else textPrimary.copy(alpha = 0.6f),
        )
    }
}
