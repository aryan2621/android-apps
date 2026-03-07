package com.example.coollauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coollauncher.data.AppInfo
import com.example.coollauncher.ui.AppIcon
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.LocalHardMinimalMode
import com.example.coollauncher.ui.theme.surfaceElevated
import com.example.coollauncher.ui.theme.textPrimary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.example.coollauncher.ui.AppIcons

private val CELL_ICON_SIZE = 56.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridCell(
    app: AppInfo,
    onLaunch: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null,
) {
    val accentColors = LocalAccentColors.current
    val hardMinimalMode = LocalHardMinimalMode.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.94f else 1f

    val effectiveOnClick = when {
        isSelectionMode && onToggleSelect != null -> onToggleSelect
        else -> onLaunch
    }
    val effectiveOnLongClick = if (isSelectionMode) null else onLongClick

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .scale(scale)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(AppShapes.chip)
                .background(
                    if (isSelected) accentColors.primary.copy(alpha = 0.2f)
                    else surfaceElevated.copy(alpha = 0.65f)
                )
                .border(
                    1.dp,
                    if (isSelected) accentColors.primary else accentColors.primary.copy(alpha = 0.12f),
                    AppShapes.chip,
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = effectiveOnClick,
                    onLongClick = effectiveOnLongClick,
                )
                .padding(if (hardMinimalMode) 10.dp else 12.dp, if (hardMinimalMode) 10.dp else 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
        if (!hardMinimalMode) {
            AppIcon(
                drawable = app.icon,
                size = CELL_ICON_SIZE,
            )
            Text(
                text = app.label.toString(),
                style = AppTypography.caption(),
                color = textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        } else {
            Text(
                text = app.label.toString(),
                style = AppTypography.bodySmall(),
                color = textPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        }
        if (isSelectionMode && isSelected) {
            Icon(
                painter = painterResource(AppIcons.Check),
                contentDescription = null,
                tint = accentColors.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(20.dp),
            )
        }
    }
}
