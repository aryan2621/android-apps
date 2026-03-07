package com.example.coollauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.LocalAccentColors
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

private const val LAUNCH_ANIM_DURATION_MS = 1000
private const val LAUNCH_ANIM_SCALE_START = 0.25f
private const val LAUNCH_ANIM_SCALE_END = 1.2f
private const val LAUNCH_ANIM_ROTATION_DEG = 720f // two full turns
private val DOT_SIZE = 14.dp
private val DOTS_BOX_SIZE = 80.dp

@Composable
fun LaunchOverlay(
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColors = LocalAccentColors.current
    val scale = remember { Animatable(LAUNCH_ANIM_SCALE_START) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        coroutineScope {
            val scaleJob = async {
                scale.animateTo(
                    targetValue = LAUNCH_ANIM_SCALE_END,
                    animationSpec = tween(LAUNCH_ANIM_DURATION_MS),
                )
            }
            val rotationJob = async {
                rotation.animateTo(
                    targetValue = LAUNCH_ANIM_ROTATION_DEG,
                    animationSpec = tween(LAUNCH_ANIM_DURATION_MS),
                )
            }
            scaleJob.await()
            rotationJob.await()
        }
        delay(80)
        onAnimationEnd()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(DOTS_BOX_SIZE)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                },
            contentAlignment = Alignment.Center,
        ) {
            // Four dots in 2x2 grid
            FourDots(color = accentColors.primary)
        }
    }
}

@Composable
private fun BoxScope.FourDots(
    color: Color,
    dotSize: Dp = DOT_SIZE,
) {
    val quarter = 20.dp // DOTS_BOX_SIZE / 4 = 20.dp
    val halfDot = 7.dp  // dotSize / 2 for 14.dp
    // Centers at (-20,-20), (20,-20), (-20,20), (20,20); offset = center - halfDot
    val positions = listOf(
        Pair(-quarter - halfDot, -quarter - halfDot),
        Pair(quarter - halfDot, -quarter - halfDot),
        Pair(-quarter - halfDot, quarter - halfDot),
        Pair(quarter - halfDot, quarter - halfDot),
    )
    positions.forEach { (dx, dy) ->
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(dx, dy)
                .size(dotSize)
                .clip(CircleShape)
                .background(color),
        )
    }
}
