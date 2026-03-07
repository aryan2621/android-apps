package com.example.coollauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.surfaceCard
import com.example.coollauncher.ui.theme.textPrimary
import androidx.compose.material3.Text

@Composable
fun CustomDialog(
    onDismissRequest: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null,
) {
    val config = LocalConfiguration.current
    val cardMaxWidth: Dp = minOf(400.dp, config.screenWidthDp.dp * 0.9f)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = modifier
                .wrapContentSize()
                .widthIn(min = 280.dp, max = cardMaxWidth)
                .wrapContentHeight()
                .shadow(28.dp, AppShapes.dialog, spotColor = Color.Black.copy(alpha = 0.35f))
                .clip(AppShapes.dialog)
                .background(surfaceCard),
        ) {
            val accentColors = LocalAccentColors.current
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(accentColors.primary, accentColors.primary.copy(alpha = 0.6f)),
                            ),
                            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        ),
                )
                Text(
                    text = title,
                    style = AppTypography.headline(),
                    color = textPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )
                Box(modifier = Modifier.padding(horizontal = 24.dp)) { content() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dismissButton != null) {
                        dismissButton()
                        if (confirmButton != null) {
                            Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                        }
                    }
                    if (confirmButton != null) {
                        confirmButton()
                    }
                }
            }
        }
    }
}
