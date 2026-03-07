package com.example.coollauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.LocalAccentColors

private val FAB_SIZE = 58.dp

@Composable
fun PrimaryFAB(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val accentColors = LocalAccentColors.current
    Box(
        modifier = modifier
            .size(FAB_SIZE)
            .shadow(16.dp, AppShapes.pill, spotColor = accentColors.glow)
            .clip(AppShapes.pill)
            .background(accentColors.primary)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        icon()
    }
}
