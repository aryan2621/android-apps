package com.example.coollauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.borderSubtle
import com.example.coollauncher.ui.theme.surfaceCard
import com.example.coollauncher.ui.theme.textPrimary

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .shadow(4.dp, AppShapes.pillLarge, spotColor = Color.Black.copy(alpha = 0.15f))
            .clip(AppShapes.pillLarge)
            .background(surfaceCard)
            .border(1.dp, borderSubtle, AppShapes.pillLarge)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = text,
            style = AppTypography.button(),
            color = if (enabled) textPrimary else textPrimary.copy(alpha = 0.5f),
        )
    }
}
