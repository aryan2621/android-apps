package com.example.coollauncher.ui

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import android.widget.ImageView

/** Rounded square clip for app icons (adaptive-icon style). */
private val AppIconShape = RoundedCornerShape(12.dp)

@Composable
fun AppIcon(
    drawable: Drawable,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    val context = LocalContext.current
    val fallbackDrawable = remember(context) {
        ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)
    }
    val iconDrawable = drawable.takeIf { it.constantState != null } ?: fallbackDrawable
    if (iconDrawable == null) return
    AndroidView(
        modifier = modifier
            .size(size)
            .clip(AppIconShape),
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageDrawable(iconDrawable)
            }
        },
        update = { imageView ->
            imageView.setImageDrawable(iconDrawable)
        },
    )
}
