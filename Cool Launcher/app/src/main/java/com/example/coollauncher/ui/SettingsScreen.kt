package com.example.coollauncher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.components.CustomDialog
import com.example.coollauncher.ui.components.FrostedCard
import com.example.coollauncher.ui.components.PrimaryButton
import com.example.coollauncher.ui.components.SecondaryButton
import com.example.coollauncher.ui.components.SecondaryIconButton
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.accentColorsFor
import com.example.coollauncher.ui.theme.backgroundDark
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.borderSubtle
import com.example.coollauncher.ui.theme.surfaceElevated
import com.example.coollauncher.ui.theme.textPrimary
import com.example.coollauncher.ui.theme.textSecondary
import com.example.coollauncher.util.openAppInfoForUninstall
import com.example.coollauncher.util.openDefaultAppsSettings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    fontKey: Int,
    colorThemeKey: Int,
    hardMinimalMode: Boolean,
    onFontKeyChange: (Int) -> Unit,
    onColorThemeChange: (Int) -> Unit,
    onHardMinimalModeChange: (Boolean) -> Unit,
    onRequestShowTour: () -> Unit = {},
    onResetSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val accentColors = LocalAccentColors.current
    val context = LocalContext.current
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundDark)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 40.dp, end = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SecondaryIconButton(
                onClick = onBack,
                icon = { Icon(painter = painterResource(AppIcons.ArrowBack), contentDescription = null, tint = textSecondary) },
                contentDescription = "Back",
            )
            Text(
                text = "Settings",
                style = AppTypography.headline(),
                color = textPrimary,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            FrostedCard(modifier = Modifier.fillMaxWidth()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    listOf(
                        0 to "Teal",
                        1 to "Orange",
                        2 to "Red",
                        3 to "Green",
                        4 to "Yellow",
                        5 to "Blue",
                        6 to "Purple",
                    ).forEach { (key, label) ->
                        val accent = accentColorsFor(key)
                        val isSelected = colorThemeKey == key
                        Box(
                            modifier = Modifier
                                .clip(AppShapes.chip)
                                .background(
                                    if (isSelected) accent.primary.copy(alpha = 0.22f)
                                    else surfaceElevated.copy(alpha = 0.8f),
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) accent.primary else borderSubtle,
                                    AppShapes.chip,
                                )
                                .clickable { onColorThemeChange(key) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(accent.primary),
                                )
                                Text(
                                    text = label,
                                    style = AppTypography.bodySmall(),
                                    color = if (isSelected) accent.primary else textPrimary,
                                )
                            }
                        }
                    }
                }
            }

            FrostedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(
                        0 to "Default",
                        1 to "Serif",
                        2 to "Monospace",
                    ).forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onFontKeyChange(key) }
                                .padding(12.dp, 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(AppIcons.FormatSize),
                                    contentDescription = null,
                                    tint = textSecondary,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.size(12.dp))
                                Text(
                                    text = label,
                                    style = AppTypography.body(),
                                    color = textPrimary,
                                )
                            }
                            if (fontKey == key) {
                                Icon(
                                    painter = painterResource(AppIcons.Check),
                                    contentDescription = null,
                                    tint = accentColors.primary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }

            FrostedCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Hard minimal mode",
                            style = AppTypography.body(),
                            color = textPrimary,
                        )
                        Text(
                            text = "Show app names only, no icons",
                            style = AppTypography.caption(),
                            color = textSecondary,
                        )
                    }
                    Switch(
                        checked = hardMinimalMode,
                        onCheckedChange = onHardMinimalModeChange,
                    )
                }
            }

            FrostedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showResetConfirmDialog = true },
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Replay),
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Reset launcher settings",
                        style = AppTypography.body(),
                        color = textPrimary,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FrostedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable {
                                onRequestShowTour()
                                onBack()
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Replay),
                            contentDescription = "Show tour again",
                            tint = textSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                FrostedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable { context.openDefaultAppsSettings() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Home),
                            contentDescription = "Set as default launcher",
                            tint = textSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                FrostedCard(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .clickable { context.openAppInfoForUninstall() }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.DeleteForever),
                            contentDescription = "Uninstall Cool Launcher",
                            tint = textSecondary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showResetConfirmDialog) {
            CustomDialog(
                onDismissRequest = { showResetConfirmDialog = false },
                title = "Reset launcher settings?",
                content = {
                    Text(
                        text = "Theme, font, color, minimal mode and tour will be reset to default.",
                        style = AppTypography.body(),
                        color = textSecondary,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                dismissButton = {
                    SecondaryButton(
                        text = "Cancel",
                        onClick = { showResetConfirmDialog = false },
                    )
                },
                confirmButton = {
                    PrimaryButton(
                        text = "Reset",
                        onClick = {
                            onResetSettings()
                            showResetConfirmDialog = false
                        },
                    )
                },
            )
        }
    }
}
