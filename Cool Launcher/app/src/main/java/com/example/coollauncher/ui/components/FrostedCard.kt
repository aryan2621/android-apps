package com.example.coollauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.borderSubtle
import com.example.coollauncher.ui.theme.surfaceCard

@Composable
fun FrostedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = AppShapes.card,
                spotColor = Color.Black.copy(alpha = 0.28f),
                ambientColor = Color.Black.copy(alpha = 0.12f),
            )
            .clip(AppShapes.card)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        surfaceCard.copy(alpha = 0.98f),
                        surfaceCard,
                    ),
                ),
            )
            .then(
                Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = 120f,
                    ),
                    alpha = 1f,
                )
            )
            .clip(AppShapes.card)
            .border(1.dp, borderSubtle.copy(alpha = 0.8f), AppShapes.card),
        content = { content() },
    )
}
