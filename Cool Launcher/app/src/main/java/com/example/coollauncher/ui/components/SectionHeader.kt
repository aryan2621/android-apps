package com.example.coollauncher.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.AppTypography
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.textSecondary
import androidx.compose.material3.Text

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SectionHeader(
    name: String,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = { },
                        onLongClick = onLongClick,
                    )
                } else Modifier
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .background(
                    LocalAccentColors.current.primary.copy(alpha = 0.8f),
                    RoundedCornerShape(2.dp),
                ),
        )
        Text(
            text = name,
            style = AppTypography.sectionTitle(),
            color = textSecondary,
        )
    }
}
