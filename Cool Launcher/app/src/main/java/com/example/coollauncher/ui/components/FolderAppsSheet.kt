package com.example.coollauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.coollauncher.data.AppInfo
import com.example.coollauncher.ui.AppIcon
import androidx.compose.ui.draw.shadow
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalHardMinimalMode
import com.example.coollauncher.ui.theme.borderSubtle
import com.example.coollauncher.ui.theme.surfaceCard
import com.example.coollauncher.ui.theme.textPrimary
import androidx.compose.material3.Text

private val SHEET_APP_ICON_SIZE = 56.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderAppsSheet(
    folderName: String,
    apps: List<AppInfo>,
    onDismiss: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onAppLongClick: (AppInfo) -> Unit,
    modifier: Modifier = Modifier,
    closeIcon: @Composable () -> Unit,
) {
    val accentColors = com.example.coollauncher.ui.theme.LocalAccentColors.current
    val hardMinimalMode = LocalHardMinimalMode.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(20.dp, AppShapes.cardLarge)
            .clip(AppShapes.cardLarge)
            .background(surfaceCard)
            .then(Modifier.border(1.dp, borderSubtle.copy(alpha = 0.8f), AppShapes.cardLarge))
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .padding(2.dp)
                        .background(
                            accentColors.primary.copy(alpha = 0.5f),
                            androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                        ),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = folderName,
                    style = AppTypography.headline(),
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .combinedClickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    closeIcon()
                }
            }
            if (apps.isEmpty()) {
                Text(
                    text = "No apps in this folder",
                    style = AppTypography.body(),
                    color = textPrimary,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(if (hardMinimalMode) 8.dp else 16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    apps.forEach { app ->
                        Column(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = { onAppClick(app) },
                                    onLongClick = { onAppLongClick(app) },
                                )
                                .padding(if (hardMinimalMode) 6.dp else 8.dp)
                                .widthIn(min = if (hardMinimalMode) 48.dp else 72.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (!hardMinimalMode) {
                                AppIcon(
                                    drawable = app.icon,
                                    size = SHEET_APP_ICON_SIZE,
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
                    }
                }
            }
        }
    }
}
