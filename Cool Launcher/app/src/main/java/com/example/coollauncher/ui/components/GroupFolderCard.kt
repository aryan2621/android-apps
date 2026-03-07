package com.example.coollauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import com.example.coollauncher.data.AppInfo
import com.example.coollauncher.ui.AppIcon
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalHardMinimalMode
import com.example.coollauncher.ui.theme.textPrimary
import com.example.coollauncher.ui.theme.textSecondary

private const val MAX_PREVIEW_ICONS = 8
private val PREVIEW_ICON_SIZE = 40.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupFolderCard(
    groupName: String,
    apps: List<AppInfo>,
    onFolderClick: () -> Unit,
    onGroupLongClick: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hardMinimalMode = LocalHardMinimalMode.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f

    FrostedCard(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .aspectRatio(1.15f)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onFolderClick,
                onLongClick = onGroupLongClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = groupName,
                style = AppTypography.sectionTitle(),
                color = textPrimary,
            )
            if (apps.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (hardMinimalMode) 6.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    apps.take(MAX_PREVIEW_ICONS).forEach { app ->
                        Box(
                            modifier = Modifier
                                .then(
                                    if (hardMinimalMode) Modifier
                                    else Modifier.size(PREVIEW_ICON_SIZE)
                                )
                                .combinedClickable(
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) },
                                )
                                .padding(if (hardMinimalMode) 4.dp else 0.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (hardMinimalMode) {
                                Text(
                                    text = app.label.toString(),
                                    style = AppTypography.caption(),
                                    color = textPrimary,
                                    maxLines = 1,
                                )
                            } else {
                                AppIcon(
                                    drawable = app.icon,
                                    size = PREVIEW_ICON_SIZE,
                                )
                            }
                        }
                    }
                }
            } else {
                Text(
                    text = "No apps — long-press an app in All apps to add",
                    style = AppTypography.caption(),
                    color = textSecondary,
                )
            }
        }
    }
}
