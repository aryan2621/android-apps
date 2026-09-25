package com.instashow.render

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import com.instashow.health.GeoPoint
import com.instashow.health.StatFormat
import com.instashow.story.StoryData
import com.instashow.template.GlassPanel
import com.instashow.template.PhotoSlot
import com.instashow.template.QuoteSlot
import com.instashow.template.RingSlot
import com.instashow.template.RouteSlot
import com.instashow.template.ScrimSlot
import com.instashow.template.ShapeSlot
import com.instashow.template.StatGrid
import com.instashow.template.StatSlot
import com.instashow.template.StoryTemplate
import com.instashow.template.TemplateBackground
import com.instashow.template.TextSlot
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import android.graphics.Color as AndroidColor

class StoryRenderer(private val fonts: StoryFonts) {
    fun render(
        template: StoryTemplate,
        photo: Bitmap?,
        data: StoryData,
        style: StoryStyle = StoryStyle(),
        width: Int = STORY_WIDTH,
        height: Int = STORY_HEIGHT,
    ): Bitmap {
        require(width > 0 && height > 0)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val frameColor = style.backgroundColor
        if (frameColor == null) {
            drawContent(canvas, bitmap, template, photo, data, style, width, height)
            return bitmap
        }
        canvas.drawColor(frameColor)
        val content = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            drawContent(Canvas(content), content, template, photo, data, style, width, height)
            val fit = style.frameScale.coerceIn(0.62f, 0.92f)
            val dest = RectF(
                width * (1f - fit) / 2f,
                height * (1f - fit) / 2f,
                width * (1f + fit) / 2f,
                height * (1f + fit) / 2f,
            )
            val radius = (56f * width / STORY_WIDTH.toFloat() * fit).coerceAtMost(dest.width() / 2f)
            canvas.save()
            canvas.clipPath(Path().apply { addRoundRect(dest, radius, radius, Path.Direction.CW) })
            canvas.drawBitmap(content, null, dest, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            canvas.restore()
        } finally {
            if (!content.isRecycled) content.recycle()
        }
        return bitmap
    }

    private fun drawContent(
        canvas: Canvas,
        bitmap: Bitmap,
        template: StoryTemplate,
        photo: Bitmap?,
        data: StoryData,
        style: StoryStyle,
        width: Int,
        height: Int,
    ) {
        val scale = width / STORY_WIDTH.toFloat()
        val typeScale = style.textScale.coerceIn(0.8f, 1.75f)
        drawBackground(canvas, template.background, width, height)
        template.shapes.filter { it.layer == "back" }.forEach { drawShape(canvas, it, data, width, height, scale) }
        if (photo != null && template.photo.shape != "none") {
            drawPhoto(canvas, template, photo, style, width, height, scale)
        }
        template.scrim?.let { drawScrim(canvas, it, width, height) }
        if (template.grain > 0f) drawGrain(canvas, template.grain, width, height)
        template.panels.forEach { drawPanel(canvas, bitmap, it, scale) }
        template.shapes.filter { it.layer == "front" }.forEach { drawShape(canvas, it, data, width, height, scale) }
        template.route?.let { slot -> data.route.takeIf { it.size >= 2 }?.let { drawRoute(canvas, slot, it, width, height, scale) } }
        template.rings.forEach { drawRing(canvas, it, data, width, height, scale) }
        template.quote?.let { drawQuote(canvas, it, data, style, width, height, scale, typeScale) }
        template.texts.forEach { drawText(canvas, it, data, style.textColor, style.headline.takeIf { _ -> template.quote == null }, width, height, scale, typeScale) }
        template.stats.forEach { slot -> drawStat(canvas, slot, data, width, height, scale, typeScale, style.textColor) }
        template.grid?.let { drawGrid(canvas, it, data, width, height, scale, typeScale, style.textColor) }
    }

    private fun drawGrid(
        canvas: Canvas,
        grid: StatGrid,
        data: StoryData,
        width: Int,
        height: Int,
        scale: Float,
        textScale: Float,
        textColor: Int?,
    ) {
        val keys = StatFormat.supportingKeys.drop(grid.from)
            .filter { StatFormat.value(it, data).available }
            .take(grid.columns * grid.rows)
        val cellWidth = grid.width / grid.columns
        val usedRows = (keys.size + grid.columns - 1) / grid.columns
        grid.divider?.let { divider ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.parseColor(divider)
                strokeWidth = 2f * scale
            }
            for (row in 1 until usedRows) {
                val y = (grid.top + row * grid.rowHeight - grid.rowHeight * 0.12f) * height
                canvas.drawLine(grid.left * width, y, (grid.left + grid.width) * width, y, paint)
            }
        }
        keys.forEachIndexed { index, key ->
            val slot = StatSlot(
                stat = key,
                left = grid.left + (index % grid.columns) * cellWidth,
                top = grid.top + (index / grid.columns) * grid.rowHeight,
                valueSize = grid.valueSize,
                labelSize = grid.labelSize,
                color = grid.color,
                font = grid.font,
                unitScale = grid.unitScale,
                labelAbove = grid.labelAbove,
                accent = grid.accent,
            )
            drawStat(canvas, slot, data, width, height, scale, textScale, textColor, maxWidth = cellWidth * 0.94f)
        }
    }

    private fun drawBackground(canvas: Canvas, background: TemplateBackground, width: Int, height: Int) {
        val colors = background.colors.map(AndroidColor::parseColor)
        if (background.type == "solid" || colors.size < 2) {
            canvas.drawColor(colors.first())
            return
        }
        if (background.type == "mesh") {
            canvas.drawColor(colors.first())
            colors.drop(1).forEachIndexed { index, color ->
                val (fx, fy) = MESH_POINTS[index % MESH_POINTS.size]
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        fx * width,
                        fy * height,
                        width * 0.95f,
                        intArrayOf(color, withAlpha(color, 0.45f), withAlpha(color, 0f)),
                        floatArrayOf(0f, 0.45f, 1f),
                        Shader.TileMode.CLAMP,
                    )
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
            return
        }
        val points = gradientPoints(background.angle, width, height)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(points[0], points[1], points[2], points[3], colors.toIntArray(), null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawPhoto(
        canvas: Canvas,
        template: StoryTemplate,
        photo: Bitmap,
        storyStyle: StoryStyle,
        width: Int,
        height: Int,
        scale: Float,
    ) {
        val slot = template.photo
        val filter = template.filter
        val rect = photoRect(slot, width, height)
        if (rect.width() <= 0f || rect.height() <= 0f) return
        val outer = canvas.save()
        if (slot.rotate != 0f) canvas.rotate(slot.rotate, rect.centerX(), rect.centerY())
        val border = slot.borderWidth * scale
        val framed = RectF(rect.left - border, rect.top - border, rect.right + border, rect.bottom + border)
        if (slot.shadow) {
            canvas.drawPath(
                photoPath(RectF(framed.left, framed.top + 18f * scale, framed.right, framed.bottom + 18f * scale), slot.shape, slot.radius * scale),
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = 0x70000000
                    maskFilter = BlurMaskFilter(40f * scale, BlurMaskFilter.Blur.NORMAL)
                },
            )
        }
        if (slot.border != null && border > 0f) {
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = AndroidColor.parseColor(slot.border) }
            canvas.drawPath(photoPath(framed, slot.shape, slot.radius * scale + border), borderPaint)
        }
        canvas.save()
        canvas.clipPath(photoPath(rect, slot.shape, slot.radius * scale))
        drawCrop(canvas, photo, rect, storyStyle, filterPaint(filter, template.duotone))
        template.overlay?.let { canvas.drawColor(AndroidColor.parseColor(it)) }
        if (template.vignette > 0f) drawVignette(canvas, rect, template.vignette)
        canvas.restore()
        canvas.restoreToCount(outer)
        if (slot.shape == "circle" && slot.border == null) {
            canvas.drawOval(
                rect,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 4f * scale
                    color = 0x66FFFFFF
                },
            )
        }
    }

    private fun filterPaint(filter: String, duotone: List<String> = emptyList()): Paint {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        val matrix = when (filter) {
            "duotone" -> duotoneMatrix(duotone)
            "mono" -> ColorMatrix().apply { setSaturation(0f) }.also { it.postConcat(contrast(1.12f)) }
            "warm" -> ColorMatrix(
                floatArrayOf(
                    1.08f, 0f, 0f, 0f, 6f,
                    0f, 1.0f, 0f, 0f, 2f,
                    0f, 0f, 0.88f, 0f, -4f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            ).also { it.postConcat(ColorMatrix().apply { setSaturation(1.08f) }) }
            "cool" -> ColorMatrix(
                floatArrayOf(
                    0.92f, 0f, 0f, 0f, -2f,
                    0f, 1.0f, 0f, 0f, 2f,
                    0f, 0f, 1.1f, 0f, 8f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
            "fade" -> contrast(0.84f).also { it.postConcat(ColorMatrix().apply { setSaturation(0.82f) }) }
            "punch" -> ColorMatrix().apply { setSaturation(1.28f) }.also { it.postConcat(contrast(1.14f)) }
            else -> null
        }
        if (matrix != null) paint.colorFilter = ColorMatrixColorFilter(matrix)
        return paint
    }

    /** Maps brightness onto a line between two colors: shadows take the first, highlights the second. */
    private fun duotoneMatrix(colors: List<String>): ColorMatrix? {
        if (colors.size != 2) return null
        val dark = AndroidColor.parseColor(colors[0])
        val light = AndroidColor.parseColor(colors[1])
        fun row(d: Int, l: Int): FloatArray {
            val k = (l - d) / 255f
            return floatArrayOf(k * 0.299f, k * 0.587f, k * 0.114f, 0f, d.toFloat())
        }
        val matrix = ColorMatrix(
            row(AndroidColor.red(dark), AndroidColor.red(light)) +
                row(AndroidColor.green(dark), AndroidColor.green(light)) +
                row(AndroidColor.blue(dark), AndroidColor.blue(light)) +
                floatArrayOf(0f, 0f, 0f, 1f, 0f),
        )
        return contrast(1.18f).also { it.postConcat(matrix) }
    }

    private fun contrast(amount: Float): ColorMatrix {
        val translate = (1f - amount) * 128f
        return ColorMatrix(
            floatArrayOf(
                amount, 0f, 0f, 0f, translate,
                0f, amount, 0f, 0f, translate,
                0f, 0f, amount, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
    }

    private fun drawVignette(canvas: Canvas, rect: RectF, strength: Float) {
        val radius = hypot(rect.width(), rect.height()) / 2f
        val edge = AndroidColor.argb((strength.coerceIn(0f, 1f) * 255).roundToInt(), 0, 0, 0)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                rect.centerX(),
                rect.centerY(),
                radius,
                intArrayOf(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT, edge),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRect(rect, paint)
    }

    private fun photoRect(slot: PhotoSlot, width: Int, height: Int): RectF = RectF(
        slot.left * width,
        slot.top * height,
        (slot.left + slot.width) * width,
        (slot.top + slot.height) * height,
    )

    private fun photoPath(rect: RectF, shape: String, cornerRadius: Float): Path = Path().apply {
        when (shape) {
            "circle" -> addOval(rect, Path.Direction.CW)
            "arch" -> {
                val top = rect.width() / 2f
                val bottom = cornerRadius
                addRoundRect(rect, floatArrayOf(top, top, top, top, bottom, bottom, bottom, bottom), Path.Direction.CW)
            }
            else -> addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
    }

    private val grainTile: Bitmap by lazy {
        val size = 256
        val random = java.util.Random(7)
        val pixels = IntArray(size * size) {
            val v = random.nextInt(256)
            AndroidColor.argb(random.nextInt(90), v, v, v)
        }
        Bitmap.createBitmap(pixels, size, size, Bitmap.Config.ARGB_8888)
    }

    private fun drawGrain(canvas: Canvas, amount: Float, width: Int, height: Int) {
        val paint = Paint().apply {
            shader = BitmapShader(grainTile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
            alpha = (amount.coerceIn(0f, 1f) * 255).roundToInt()
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }

    private fun drawShape(canvas: Canvas, shape: ShapeSlot, data: StoryData, width: Int, height: Int, scale: Float) {
        val rect = RectF(
            shape.left * width,
            shape.top * height,
            (shape.left + shape.width) * width,
            (shape.top + shape.height) * height,
        )
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = withAlpha(AndroidColor.parseColor(shape.color), shape.alpha)
            if (shape.stroke > 0f) {
                style = Paint.Style.STROKE
                strokeWidth = shape.stroke * scale
            }
            if (shape.dash) {
                val dash = max(shape.stroke, 3f) * scale * 3f
                pathEffect = DashPathEffect(floatArrayOf(dash, dash * 0.8f), 0f)
            }
        }
        val saved = canvas.save()
        if (shape.rotate != 0f) canvas.rotate(shape.rotate, rect.centerX(), rect.centerY())
        when (shape.type) {
            "rect" -> canvas.drawRoundRect(rect, shape.radius * scale, shape.radius * scale, paint)
            "circle" -> canvas.drawOval(rect, paint)
            "line" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(shape.stroke, 2f) * scale
                canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, paint)
            }
            "barcode" -> {
                paint.style = Paint.Style.FILL
                val random = java.util.Random(data.date.toEpochDay() + (data.workout?.start?.epochSecond ?: 0L))
                val unit = rect.width() / 95f
                var x = rect.left
                var bar = true
                while (x < rect.right) {
                    val w = unit * (1 + random.nextInt(3))
                    if (bar) canvas.drawRect(x, rect.top, (x + w).coerceAtMost(rect.right), rect.bottom, paint)
                    x += w
                    bar = !bar
                }
            }
            "grid" -> {
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(shape.stroke, 1.5f) * scale
                val n = shape.count.coerceIn(1, 40)
                for (i in 0..n) {
                    val x = rect.left + rect.width() * i / n
                    val y = rect.top + rect.height() * i / n
                    canvas.drawLine(x, rect.top, x, rect.bottom, paint)
                    canvas.drawLine(rect.left, y, rect.right, y, paint)
                }
            }
            "horizon" -> {
                // Retro perspective floor: lines fan out from a vanishing point at the top center.
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(shape.stroke, 2f) * scale
                val n = shape.count.coerceIn(2, 30)
                canvas.save()
                canvas.clipRect(rect)
                for (i in 0..n) {
                    val t = i / n.toFloat()
                    val y = rect.top + rect.height() * t * t
                    canvas.drawLine(rect.left, y, rect.right, y, paint)
                }
                for (i in -n..n) {
                    val x = rect.centerX() + rect.width() * 1.6f * i / n
                    canvas.drawLine(rect.centerX(), rect.top, x, rect.bottom, paint)
                }
                canvas.restore()
            }
            "dots" -> {
                paint.style = Paint.Style.FILL
                val n = shape.count.coerceIn(2, 60)
                val step = rect.width() / n
                val r = step * 0.14f
                var y = rect.top + step / 2f
                while (y < rect.bottom) {
                    for (i in 0 until n) canvas.drawCircle(rect.left + step * (i + 0.5f), y, r, paint)
                    y += step
                }
            }
        }
        canvas.restoreToCount(saved)
    }

    private fun drawCrop(canvas: Canvas, bitmap: Bitmap, dest: RectF, style: StoryStyle, paint: Paint) {
        if (bitmap.width <= 0 || bitmap.height <= 0) return
        val source = PhotoCrop.sourceRect(
            bitmap.width,
            bitmap.height,
            dest.width(),
            dest.height(),
            zoom = 1f,
            centerX = 0.5f,
            centerY = 0.5f,
        )
        val src = Rect(
            source.left.roundToInt().coerceIn(0, bitmap.width - 1),
            source.top.roundToInt().coerceIn(0, bitmap.height - 1),
            source.right.roundToInt().coerceIn(1, bitmap.width),
            source.bottom.roundToInt().coerceIn(1, bitmap.height),
        )
        if (src.width() <= 0 || src.height() <= 0) return
        canvas.drawBitmap(bitmap, src, dest, paint)
    }

    private fun drawScrim(canvas: Canvas, scrim: ScrimSlot, width: Int, height: Int) {
        val band = (scrim.height * height).coerceIn(1f, height.toFloat())
        val parsed = AndroidColor.parseColor(scrim.color)
        val opaque = withAlpha(parsed, scrim.maxAlpha)
        val clear = withAlpha(parsed, 0f)
        val top: Float
        val bottom: Float
        val colors: IntArray
        if (scrim.edge == "top") {
            top = 0f
            bottom = band
            colors = intArrayOf(opaque, clear)
        } else {
            top = height - band
            bottom = height.toFloat()
            colors = intArrayOf(clear, opaque)
        }
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, top, 0f, bottom, colors, null, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, top, width.toFloat(), bottom, paint)
    }

    private fun drawPanel(canvas: Canvas, story: Bitmap, panel: GlassPanel, scale: Float) {
        val rect = RectF(
            panel.left * story.width,
            panel.top * story.height,
            (panel.left + panel.width) * story.width,
            (panel.top + panel.height) * story.height,
        )
        if (rect.width() < 2f || rect.height() < 2f) return
        val radius = (panel.radius * scale)
            .coerceAtMost(rect.width() / 2f)
            .coerceAtMost(rect.height() / 2f)
        if (panel.blur) {
            val blurred = blurRegion(story, rect)
            if (blurred != null) {
                canvas.save()
                canvas.clipPath(Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) })
                canvas.drawBitmap(blurred, null, rect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                canvas.restore()
                blurred.recycle()
            }
        }
        canvas.drawRoundRect(
            rect,
            radius,
            radius,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = withAlpha(AndroidColor.parseColor(panel.fill), panel.fillAlpha) },
        )
        if (panel.strokeAlpha > 0f) {
            canvas.drawRoundRect(
                rect,
                radius,
                radius,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2f * scale
                    color = withAlpha(AndroidColor.parseColor(panel.stroke), panel.strokeAlpha)
                },
            )
        }
    }

    private fun blurRegion(source: Bitmap, rect: RectF): Bitmap? {
        val left = rect.left.roundToInt().coerceIn(0, source.width - 1)
        val top = rect.top.roundToInt().coerceIn(0, source.height - 1)
        val right = rect.right.roundToInt().coerceIn(left + 1, source.width)
        val bottom = rect.bottom.roundToInt().coerceIn(top + 1, source.height)
        val width = right - left
        val height = bottom - top
        if (width < 2 || height < 2) return null
        val crop = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        Canvas(crop).drawBitmap(source, Rect(left, top, right, bottom), Rect(0, 0, width, height), null)
        val longest = max(width, height)
        val sample = (72f / longest).coerceAtMost(1f)
        val smallW = (width * sample).roundToInt().coerceAtLeast(1)
        val smallH = (height * sample).roundToInt().coerceAtLeast(1)
        val small = if (smallW == width && smallH == height) crop else Bitmap.createScaledBitmap(crop, smallW, smallH, true)
        if (small !== crop) crop.recycle()
        boxBlur(small, radius = 4, passes = 2)
        val up = if (small.width == width && small.height == height) {
            small
        } else {
            Bitmap.createScaledBitmap(small, width, height, true)
        }
        if (up !== small) small.recycle()
        return up
    }

    private fun boxBlur(bitmap: Bitmap, radius: Int, passes: Int) {
        val width = bitmap.width
        val height = bitmap.height
        if (width < 2 || height < 2 || radius < 1) return
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val tmp = IntArray(pixels.size)
        repeat(passes) {
            blurAxis(pixels, tmp, width, height, radius, horizontal = true)
            blurAxis(tmp, pixels, width, height, radius, horizontal = false)
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
    }

    private fun blurAxis(
        source: IntArray,
        dest: IntArray,
        width: Int,
        height: Int,
        radius: Int,
        horizontal: Boolean,
    ) {
        val kernel = radius * 2 + 1
        val length = if (horizontal) width else height
        val lines = if (horizontal) height else width
        for (line in 0 until lines) {
            var alpha = 0
            var red = 0
            var green = 0
            var blue = 0
            for (offset in -radius..radius) {
                val color = source[pixelIndex(line, offset.coerceIn(0, length - 1), width, horizontal)]
                alpha += color ushr 24
                red += (color shr 16) and 0xFF
                green += (color shr 8) and 0xFF
                blue += color and 0xFF
            }
            for (index in 0 until length) {
                dest[pixelIndex(line, index, width, horizontal)] =
                    ((alpha / kernel) shl 24) or
                    ((red / kernel) shl 16) or
                    ((green / kernel) shl 8) or
                    (blue / kernel)
                val remove = source[pixelIndex(line, (index - radius).coerceIn(0, length - 1), width, horizontal)]
                val add = source[pixelIndex(line, (index + radius + 1).coerceIn(0, length - 1), width, horizontal)]
                alpha += (add ushr 24) - (remove ushr 24)
                red += ((add shr 16) and 0xFF) - ((remove shr 16) and 0xFF)
                green += ((add shr 8) and 0xFF) - ((remove shr 8) and 0xFF)
                blue += (add and 0xFF) - (remove and 0xFF)
            }
        }
    }

    private fun pixelIndex(line: Int, index: Int, width: Int, horizontal: Boolean): Int =
        if (horizontal) line * width + index else index * width + line

    private fun drawQuote(
        canvas: Canvas,
        quote: QuoteSlot,
        data: StoryData,
        style: StoryStyle,
        width: Int,
        height: Int,
        scale: Float,
        textScale: Float,
    ) {
        val text = StatFormat.fill(style.headline ?: quote.text, data).trim()
        val captionText = StatFormat.fill(style.caption ?: quote.caption.orEmpty(), data).trim()
        val color = style.textColor ?: AndroidColor.parseColor(quote.color)
        val x = quote.left * width
        val maxWidth = quote.maxWidth * width
        var bottom = quote.top * height
        if (text.isNotEmpty()) {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = quote.size * scale * textScale
                typeface = when (quote.style) {
                    "italic" -> fonts.serif
                    "display" -> fonts.display
                    else -> fonts.bold
                }
                textAlign = if (quote.align == "center") Paint.Align.CENTER else Paint.Align.LEFT
                letterSpacing = quote.tracking
            }
            fitWidth(paint, text, maxWidth)
            val baseline = quote.top * height - paint.ascent()
            canvas.drawText(text, x, baseline, paint)
            bottom = baseline + paint.descent()
        }
        if (captionText.isEmpty()) return
        val captionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = (quote.size * textScale * 0.26f).coerceIn(28f, 48f) * scale
            typeface = fonts.medium
            textAlign = if (quote.align == "center") Paint.Align.CENTER else Paint.Align.LEFT
            letterSpacing = 0.02f
            alpha = (AndroidColor.alpha(color) * 0.86f).roundToInt().coerceIn(0, 255)
        }
        fitWidth(captionPaint, captionText, maxWidth)
        val gap = if (text.isEmpty()) 0f else 14f * scale
        canvas.drawText(captionText, x, bottom + gap - captionPaint.ascent(), captionPaint)
    }

    private fun drawText(
        canvas: Canvas,
        slot: TextSlot,
        data: StoryData,
        textColor: Int?,
        headline: String?,
        width: Int,
        height: Int,
        scale: Float,
        textScale: Float,
    ) {
        if (slot.modes.isNotEmpty() && (if (data.isWorkout) "workout" else "day") !in slot.modes) return
        val source = if (slot.editable && headline != null) headline else slot.text
        val text = StatFormat.fill(source, data).trim()
        // A line built around a stat the story doesn't have is left out rather than showing a dash.
        if (text.isEmpty() || StatFormat.UNAVAILABLE in text) return
        val pillColor = slot.pill?.let(AndroidColor::parseColor)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (pillColor == null) textColor ?: AndroidColor.parseColor(slot.color) else AndroidColor.parseColor(slot.color)
            alpha = (AndroidColor.alpha(color) * slot.alpha.coerceIn(0f, 1f)).roundToInt()
            textSize = slot.size * scale * textScale
            typeface = fonts.named(slot.font)
            letterSpacing = slot.tracking
            textAlign = Paint.Align.LEFT
            if (slot.outline) {
                style = Paint.Style.STROKE
                strokeWidth = textSize * 0.028f
            }
            if (slot.shadow) setShadowLayer(textSize * 0.14f, 0f, textSize * 0.04f, 0x99000000.toInt())
        }
        val fit = if (slot.maxWidth > 0f) width * slot.maxWidth else availableWidth(slot.align, slot.left, width)
        fitWidth(paint, text, fit)
        val textWidth = paint.measureText(text)
        val anchor = slot.left * width
        val startX = when (slot.align) {
            "center" -> anchor - textWidth / 2f
            "right" -> anchor - textWidth
            else -> anchor
        }
        val top = slot.top * height
        val saved = canvas.save()
        if (slot.rotate != 0f) canvas.rotate(slot.rotate, anchor, top)
        if (slot.repeat > 1 && pillColor == null) {
            val step = paint.textSize * slot.repeatGap
            val echo = Paint(paint).apply {
                style = Paint.Style.STROKE
                strokeWidth = textSize * 0.022f
            }
            for (i in 0 until slot.repeat) {
                val y = top + step * i - paint.ascent()
                val last = i == slot.repeat - 1
                canvas.drawText(text, startX, y, if (last && !slot.outline) paint else echo)
            }
            canvas.restoreToCount(saved)
            return
        }
        if (pillColor != null) {
            val padX = paint.textSize * 0.62f
            val padY = paint.textSize * 0.36f
            val metrics = paint.fontMetrics
            val textHeight = metrics.descent - metrics.ascent
            val pillLeft = when (slot.align) {
                "center" -> startX - padX
                "right" -> startX - padX * 2f
                else -> startX
            }
            val rect = RectF(pillLeft, top, pillLeft + textWidth + padX * 2f, top + textHeight + padY * 2f)
            val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = if (textColor != null) textColor else pillColor
                alpha = (AndroidColor.alpha(color) * slot.pillAlpha.coerceIn(0f, 1f)).roundToInt()
            }
            canvas.drawRoundRect(rect, rect.height() / 2f, rect.height() / 2f, fill)
            canvas.drawText(text, rect.left + padX, rect.top + padY - metrics.ascent, paint)
        } else {
            canvas.drawText(text, startX, top - paint.ascent(), paint)
        }
        canvas.restoreToCount(saved)
    }

    private fun drawStat(
        canvas: Canvas,
        slot: StatSlot,
        data: StoryData,
        width: Int,
        height: Int,
        scale: Float,
        textScale: Float,
        textColor: Int?,
        maxWidth: Float? = null,
    ) {
        val value = StatFormat.value(slot.stat, data)
        if (!value.available) return
        val color = textColor ?: AndroidColor.parseColor(slot.color)
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = slot.valueSize * scale * textScale
            typeface = fonts.named(slot.font)
            textAlign = Paint.Align.LEFT
            letterSpacing = if (slot.font == "display") 0.01f else -0.01f
            if (slot.outline) {
                style = Paint.Style.STROKE
                strokeWidth = textSize * 0.024f
            }
            if (slot.shadow) setShadowLayer(textSize * 0.12f, 0f, textSize * 0.03f, 0x99000000.toInt())
        }
        val unitPaint = Paint(valuePaint).apply {
            textSize = valuePaint.textSize * slot.unitScale.coerceIn(0.2f, 1f)
            typeface = fonts.semibold
            letterSpacing = 0f
            style = Paint.Style.FILL
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = slot.accent?.let(AndroidColor::parseColor) ?: color
            if (slot.accent == null) alpha = (AndroidColor.alpha(color) * 0.78f).roundToInt().coerceIn(0, 255)
            textSize = slot.labelSize * scale * textScale
            typeface = fonts.semibold
            textAlign = Paint.Align.LEFT
            letterSpacing = 0.16f
        }
        val label = slot.label.takeUnless { it.isBlank() || it.equals("auto", ignoreCase = true) } ?: value.label
        val limit = width * when (slot.align) {
            "center" -> 0.9f
            "right" -> slot.left - 0.06f
            else -> 0.94f - slot.left
        }.coerceAtLeast(0.2f).let { limit -> maxWidth?.let { minOf(limit, it * width) } ?: limit }
        val natural = valuePaint.measureText(value.number) +
            (if (value.unit.isBlank()) 0f else valuePaint.textSize * 0.08f + unitPaint.measureText(value.unit))
        if (natural > limit && natural > 0f) {
            val shrink = limit / natural
            valuePaint.textSize *= shrink
            unitPaint.textSize *= shrink
        }
        val numberWidth = valuePaint.measureText(value.number)
        val gap = if (value.unit.isBlank()) 0f else valuePaint.textSize * 0.08f
        val unitWidth = if (value.unit.isBlank()) 0f else unitPaint.measureText(value.unit)
        val total = numberWidth + gap + unitWidth
        val anchor = slot.left * width
        fun startFor(itemWidth: Float): Float = when (slot.align) {
            "center" -> anchor - itemWidth / 2f
            "right" -> anchor - itemWidth
            else -> anchor
        }
        var cursor = slot.top * height
        val labelGap = 8f * scale
        val showLabel = label.isNotBlank() && slot.labelSize > 0f
        if (showLabel && slot.labelAbove) {
            canvas.drawText(label, startFor(labelPaint.measureText(label)), cursor - labelPaint.ascent(), labelPaint)
            cursor += labelPaint.descent() - labelPaint.ascent() + labelGap
        }
        val baseline = cursor - valuePaint.ascent() * valueAscentRatio(slot.font)
        val start = startFor(total)
        canvas.drawText(value.number, start, baseline, valuePaint)
        if (value.unit.isNotBlank()) {
            canvas.drawText(value.unit, start + numberWidth + gap, baseline, unitPaint)
        }
        if (showLabel && !slot.labelAbove) {
            val labelTop = baseline + valuePaint.descent() * 0.6f + labelGap
            canvas.drawText(label, startFor(labelPaint.measureText(label)), labelTop - labelPaint.ascent(), labelPaint)
        }
    }

    /** Anton's ascent includes a lot of headroom; trimming it keeps numbers tight to their anchor. */
    private fun valueAscentRatio(font: String): Float = if (font == "display") 0.9f else 1f

    private fun drawRoute(canvas: Canvas, slot: RouteSlot, route: List<GeoPoint>, width: Int, height: Int, scale: Float) {
        val area = RectF(slot.left * width, slot.top * height, (slot.left + slot.width) * width, (slot.top + slot.height) * height)
        val stroke = slot.stroke * scale
        val points = projectRoute(route, area, inset = stroke * 2f) ?: return
        val path = smoothPath(points)
        val color = AndroidColor.parseColor(slot.color)
        if (slot.glow) {
            canvas.drawPath(
                path,
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = stroke * 2.4f
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    this.color = withAlpha(color, 0.45f)
                    maskFilter = BlurMaskFilter(stroke * 1.6f, BlurMaskFilter.Blur.NORMAL)
                },
            )
        }
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            this.color = color
        }
        when (slot.style) {
            "dotted" -> {
                line.pathEffect = DashPathEffect(floatArrayOf(0.1f, stroke * 2.2f), 0f)
                line.strokeWidth = stroke * 1.2f
                canvas.drawPath(path, line)
            }
            "double" -> {
                line.strokeWidth = stroke * 2f
                canvas.drawPath(path, line)
                line.strokeWidth = stroke * 0.7f
                line.color = AndroidColor.WHITE
                canvas.drawPath(path, line)
            }
            else -> canvas.drawPath(path, line)
        }
        if (slot.markers) {
            val first = points.first()
            val last = points.last()
            val dot = Paint(Paint.ANTI_ALIAS_FLAG)
            dot.color = AndroidColor.WHITE
            canvas.drawCircle(first[0], first[1], stroke * 1.25f, dot)
            dot.color = color
            canvas.drawCircle(first[0], first[1], stroke * 0.7f, dot)
            dot.color = color
            canvas.drawCircle(last[0], last[1], stroke * 1.35f, dot)
            dot.color = AndroidColor.WHITE
            canvas.drawCircle(last[0], last[1], stroke * 0.6f, dot)
        }
    }

    /** Equirectangular projection fitted into [area], keeping the route's true shape. */
    private fun projectRoute(route: List<GeoPoint>, area: RectF, inset: Float): List<FloatArray>? {
        if (route.size < 2) return null
        val meanLat = route.sumOf { it.latitude } / route.size
        val kx = cos(Math.toRadians(meanLat))
        val xs = route.map { it.longitude * kx }
        val ys = route.map { -it.latitude }
        val minX = xs.min()
        val maxX = xs.max()
        val minY = ys.min()
        val maxY = ys.max()
        val spanX = (maxX - minX).coerceAtLeast(1e-9)
        val spanY = (maxY - minY).coerceAtLeast(1e-9)
        val boxW = (area.width() - inset * 2).coerceAtLeast(1f)
        val boxH = (area.height() - inset * 2).coerceAtLeast(1f)
        val fit = min(boxW / spanX, boxH / spanY)
        val offsetX = area.left + inset + (boxW - spanX * fit) / 2.0
        val offsetY = area.top + inset + (boxH - spanY * fit) / 2.0
        return route.indices.map { i ->
            floatArrayOf(
                (offsetX + (xs[i] - minX) * fit).toFloat(),
                (offsetY + (ys[i] - minY) * fit).toFloat(),
            )
        }
    }

    private fun smoothPath(points: List<FloatArray>): Path {
        val path = Path()
        path.moveTo(points[0][0], points[0][1])
        for (i in 1 until points.size - 1) {
            val midX = (points[i][0] + points[i + 1][0]) / 2f
            val midY = (points[i][1] + points[i + 1][1]) / 2f
            path.quadTo(points[i][0], points[i][1], midX, midY)
        }
        path.lineTo(points.last()[0], points.last()[1])
        return path
    }

    private fun drawRing(canvas: Canvas, slot: RingSlot, data: StoryData, width: Int, height: Int, scale: Float) {
        val steps = data.day.steps ?: 0L
        val progress = (steps.toFloat() / data.stepGoal.coerceAtLeast(1)).coerceIn(0f, 1f)
        val cx = slot.centerX * width
        val cy = slot.centerY * height
        val radius = slot.radius * width
        val rect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)
        val stroke = slot.stroke * scale
        canvas.drawOval(
            rect,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = stroke
                color = withAlpha(AndroidColor.parseColor(slot.track), slot.trackAlpha)
            },
        )
        if (progress <= 0f) return
        canvas.drawArc(
            rect,
            -90f,
            360f * progress,
            false,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = stroke
                strokeCap = Paint.Cap.ROUND
                color = AndroidColor.parseColor(slot.color)
            },
        )
    }

    private fun availableWidth(align: String, left: Float, width: Int): Float = width * when (align) {
        "center" -> 0.9f
        "right" -> left - 0.05f
        else -> 0.95f - left
    }.coerceAtLeast(0.2f)

    private fun fitWidth(paint: Paint, text: String, maxWidth: Float) {
        val measured = paint.measureText(text)
        if (measured > maxWidth && measured > 0f) paint.textSize *= maxWidth / measured
    }

    private fun withAlpha(color: Int, alpha: Float): Int {
        val channel = (alpha.coerceIn(0f, 1f) * AndroidColor.alpha(color)).roundToInt()
        return AndroidColor.argb(channel, AndroidColor.red(color), AndroidColor.green(color), AndroidColor.blue(color))
    }

    private fun gradientPoints(angle: Float, width: Int, height: Int): FloatArray {
        val radians = Math.toRadians(angle.toDouble() - 90.0)
        val dx = cos(radians).toFloat()
        val dy = sin(radians).toFloat()
        val half = hypot(width.toFloat(), height.toFloat()) / 2f
        val cx = width / 2f
        val cy = height / 2f
        return floatArrayOf(cx - dx * half, cy - dy * half, cx + dx * half, cy + dy * half)
    }

    companion object {
        const val STORY_WIDTH = 1080
        const val STORY_HEIGHT = 1920
        const val PREVIEW_WIDTH = 720
        const val PREVIEW_HEIGHT = 1280

        /** Where mesh-gradient blobs sit, as fractions of the canvas. */
        private val MESH_POINTS = listOf(0.1f to 0.15f, 0.9f to 0.35f, 0.25f to 0.8f, 0.85f to 0.9f, 0.5f to 0.5f)
    }
}
