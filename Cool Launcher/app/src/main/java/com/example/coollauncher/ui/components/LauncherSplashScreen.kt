package com.example.coollauncher.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.coollauncher.ui.theme.LocalAccentColors
import com.example.coollauncher.ui.theme.backgroundDark
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val SPLASH_DURATION_MS = 3500
private const val SCALE_START = 0.06f   // start tiny = "coming out of screen"
private const val SCALE_END = 1.35f
private const val ROTATION_DEG = 1080f  // three full turns
private val DOT_SIZE_BASE = 20.dp      // larger dots
private const val CIRCLE_RADIUS_START_DP = 10f  // small circle at start
private const val CIRCLE_RADIUS_END_DP = 52f    // large circle at end
// Diagram size: center is exactly at (size/2, size/2) = diagonal intersection of the square.
private val SPLASH_DIAGRAM_SIZE = 124.dp     // 2 * radius_end + dot_size

@Composable
fun LauncherSplashScreen(
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColors = LocalAccentColors.current
    val scale = remember { Animatable(SCALE_START) }
    val rotation = remember { Animatable(0f) }
    val radius = remember { Animatable(CIRCLE_RADIUS_START_DP) }

    LaunchedEffect(Unit) {
        coroutineScope {
            val scaleJob = async {
                scale.animateTo(
                    targetValue = SCALE_END,
                    animationSpec = tween(SPLASH_DURATION_MS),
                )
            }
            val rotationJob = async {
                rotation.animateTo(
                    targetValue = ROTATION_DEG,
                    animationSpec = tween(SPLASH_DURATION_MS),
                )
            }
            val radiusJob = async {
                radius.animateTo(
                    targetValue = CIRCLE_RADIUS_END_DP,
                    animationSpec = tween(SPLASH_DURATION_MS),
                )
            }
            scaleJob.await()
            rotationJob.await()
            radiusJob.await()
        }
        onAnimationEnd()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundDark),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(SPLASH_DIAGRAM_SIZE)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                    transformOrigin = TransformOrigin.Center
                },
            contentAlignment = Alignment.Center,
        ) {
            FourDotsCanvas(
                color = accentColors.primary,
                radiusDp = radius.value,
                dotSizeDp = DOT_SIZE_BASE,
                sizeDp = SPLASH_DIAGRAM_SIZE,
            )
        }
    }
}

/**
 * Draws four dots at the vertices of a square (on a circle). Center of the canvas is the
 * diagonal intersection; rotation is applied to the parent so it rotates around this center.
 */
@Composable
private fun FourDotsCanvas(
    color: androidx.compose.ui.graphics.Color,  // dot color
    radiusDp: Float,
    dotSizeDp: Dp,
    sizeDp: Dp,
) {
    val density = LocalDensity.current
    val radiusPx = with(density) { radiusDp.dp.toPx() }
    val dotRadiusPx = with(density) { dotSizeDp.toPx() / 2f }
    val sizePx = with(density) { sizeDp.toPx() }
    val centerX = sizePx / 2f
    val centerY = sizePx / 2f

    Canvas(modifier = Modifier.size(sizeDp)) {
        val angles = listOf(0.0, 90.0, 180.0, 270.0)
        angles.forEach { deg ->
            val rad = deg * PI / 180.0
            val dx = radiusPx * cos(rad).toFloat()
            val dy = radiusPx * sin(rad).toFloat()
            drawCircle(
                color = color,
                radius = dotRadiusPx,
                center = Offset(centerX + dx, centerY + dy),
            )
        }
    }
}
