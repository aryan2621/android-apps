package com.instashow.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.instashow.health.GeoPoint
import com.instashow.ui.theme.InstagramGradient
import com.instashow.ui.theme.Ink
import com.instashow.ui.theme.Lime
import com.instashow.ui.theme.Line
import com.instashow.ui.theme.Surface
import com.instashow.ui.theme.SurfaceHigh
import com.instashow.ui.theme.TextMuted
import com.instashow.ui.theme.TextPrimary
import com.instashow.ui.theme.TextSecondary
import kotlin.math.cos
import kotlin.math.min

enum class ActionStyle { Primary, Tonal, Instagram }

/** A round icon button with a short label under it. */
@Composable
fun CircleAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ActionStyle = ActionStyle.Tonal,
    enabled: Boolean = true,
    size: Dp = 56.dp,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val shape = CircleShape
        val base = Modifier
            .size(size)
            .clip(shape)
        val background = when (style) {
            ActionStyle.Primary -> base.background(if (enabled) Lime else Lime.copy(alpha = 0.35f))
            ActionStyle.Tonal -> base.background(SurfaceHigh).border(1.dp, Line, shape)
            ActionStyle.Instagram -> base.background(InstagramGradient, alpha = if (enabled) 1f else 0.4f)
        }
        Box(
            modifier = background
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { contentDescription = label },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(size * 0.42f),
                tint = when {
                    style == ActionStyle.Primary -> Ink
                    style == ActionStyle.Instagram -> Color.White
                    enabled -> TextPrimary
                    else -> TextMuted
                },
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (enabled) TextSecondary else TextMuted,
        )
    }
}

@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(Surface)
            .border(1.dp, Line, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        content()
    }
}

@Composable
fun Pill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) TextPrimary else Color.Transparent)
            .border(1.dp, if (selected) TextPrimary else Line, shape)
            .clickable(onClick = onClick)
            .semantics {
                this.selected = selected
                role = Role.Tab
            }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) Ink else TextSecondary,
            maxLines = 1,
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: @Composable () -> Unit = {}) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

/** Progress ring toward a goal. */
@Composable
fun GoalRing(progress: Float, modifier: Modifier = Modifier, stroke: Dp = 12.dp, color: Color = Lime) {
    Canvas(modifier = modifier) {
        val width = stroke.toPx()
        val inset = width / 2f
        val arcSize = Size(size.width - width, size.height - width)
        drawArc(
            color = color.copy(alpha = 0.16f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = arcSize,
            style = Stroke(width),
        )
        val sweep = 360f * progress.coerceIn(0f, 1f)
        if (sweep > 0f) {
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width, cap = StrokeCap.Round),
            )
        }
    }
}

/** A small drawing of a GPS route, used on workout cards. */
@Composable
fun RouteThumbnail(route: List<GeoPoint>, modifier: Modifier = Modifier, color: Color = Lime) {
    Canvas(modifier = modifier) {
        if (route.size < 2) return@Canvas
        val meanLat = route.sumOf { it.latitude } / route.size
        val kx = cos(Math.toRadians(meanLat))
        val xs = route.map { it.longitude * kx }
        val ys = route.map { -it.latitude }
        val spanX = (xs.max() - xs.min()).coerceAtLeast(1e-9)
        val spanY = (ys.max() - ys.min()).coerceAtLeast(1e-9)
        val pad = 4.dp.toPx()
        val fit = min((size.width - pad * 2) / spanX, (size.height - pad * 2) / spanY)
        val offsetX = pad + (size.width - pad * 2 - spanX * fit) / 2
        val offsetY = pad + (size.height - pad * 2 - spanY * fit) / 2
        val path = Path()
        route.indices.forEach { i ->
            val x = (offsetX + (xs[i] - xs.min()) * fit).toFloat()
            val y = (offsetY + (ys[i] - ys.min()) * fit).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

val SwatchColors = listOf(
    0xFFFFFFFF.toInt(),
    0xFF0B0B0D.toInt(),
    0xFFD7FF3A.toInt(),
    0xFFFF6B3D.toInt(),
    0xFFFF3D7F.toInt(),
    0xFF7C5CFF.toInt(),
    0xFF3DB8FF.toInt(),
    0xFF2EE59D.toInt(),
    0xFFFFD23D.toInt(),
)

/** Preset swatches plus a hue slider for anything else. */
@Composable
fun ColorPicker(
    selected: Int?,
    onPick: (Int?) -> Unit,
    hue: Float,
    onHue: (Float) -> Unit,
    onHueFinished: () -> Unit,
    autoLabel: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (autoLabel != null) {
                item {
                    Pill(text = autoLabel, selected = selected == null, onClick = { onPick(null) })
                }
            }
            items(SwatchColors) { color ->
                val isSelected = selected == color
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(2.dp, if (isSelected) TextPrimary else Color.Transparent, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                        .border(1.dp, Line, CircleShape)
                        .clickable { onPick(color) }
                        .semantics { this.selected = isSelected },
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(28.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(HueGradient)),
            )
            Slider(
                value = hue,
                onValueChange = onHue,
                onValueChangeFinished = onHueFinished,
                valueRange = 0f..360f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(hueColor(hue)),
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                ),
            )
        }
    }
}

private val HueGradient = listOf(
    Color(0xFFFF0000),
    Color(0xFFFFFF00),
    Color(0xFF00FF00),
    Color(0xFF00FFFF),
    Color(0xFF0000FF),
    Color(0xFFFF00FF),
    Color(0xFFFF0000),
)

fun hueColor(hue: Float): Int = android.graphics.Color.HSVToColor(floatArrayOf(hue.coerceIn(0f, 360f), 0.85f, 1f))

@Composable
fun AppSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        enabled = enabled,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = Lime,
            activeTrackColor = Lime,
            inactiveTrackColor = SurfaceHigh,
            disabledThumbColor = TextMuted,
            disabledActiveTrackColor = SurfaceHigh,
            disabledInactiveTrackColor = SurfaceHigh,
        ),
    )
}
