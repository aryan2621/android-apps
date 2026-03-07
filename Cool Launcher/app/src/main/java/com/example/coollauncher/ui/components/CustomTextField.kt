package com.example.coollauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.AppShapes
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.borderSubtle
import com.example.coollauncher.ui.theme.surfaceElevated
import com.example.coollauncher.ui.theme.textPrimary
import com.example.coollauncher.ui.theme.textSecondary
import androidx.compose.material3.Text

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    bottomBorderOnly: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    var focused by remember { mutableStateOf(false) }
    val accentColors = if (bottomBorderOnly) LocalAccentColors.current else null

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = AppTypography.caption(),
                color = textSecondary,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .then(
                    if (bottomBorderOnly) {
                        Modifier
                            .onFocusChanged { focused = it.isFocused }
                            .drawBehind {
                                val strokeWidth = 2.dp.toPx()
                                val lineColor = if (focused && accentColors != null) accentColors.primary else borderSubtle
                                drawLine(
                                    color = lineColor,
                                    start = Offset(0f, size.height - strokeWidth / 2f),
                                    end = Offset(size.width, size.height - strokeWidth / 2f),
                                    strokeWidth = strokeWidth,
                                )
                            }
                            .padding(horizontal = 0.dp, vertical = 12.dp)
                    } else {
                        Modifier
                            .clip(AppShapes.textField)
                            .background(surfaceElevated)
                            .border(1.5.dp, borderSubtle, AppShapes.textField)
                            .padding(horizontal = 18.dp, vertical = 16.dp)
                    }
                ),
            singleLine = singleLine,
            textStyle = AppTypography.body().copy(color = textPrimary),
            cursorBrush = SolidColor(textPrimary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = AppTypography.body(),
                            color = textSecondary,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}
