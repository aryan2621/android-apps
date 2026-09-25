package com.instashow.render

import android.graphics.RectF
import kotlin.math.max

/** Which part of a photo fills a slot: the centered window that covers it. */
object PhotoCrop {
    const val MIN_ZOOM = 1f
    const val MAX_ZOOM = 4f

    /**
     * @param centerX where the middle of the visible window sits, as a fraction of the photo width.
     * @param centerY the same for height. Both are clamped so the window never leaves the photo.
     */
    fun sourceRect(
        imageWidth: Int,
        imageHeight: Int,
        slotWidth: Float,
        slotHeight: Float,
        zoom: Float,
        centerX: Float,
        centerY: Float,
    ): RectF {
        val cover = max(slotWidth / imageWidth, slotHeight / imageHeight) * zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        val srcWidth = (slotWidth / cover).coerceAtMost(imageWidth.toFloat())
        val srcHeight = (slotHeight / cover).coerceAtMost(imageHeight.toFloat())
        val cx = clampCenter(centerX, srcWidth / imageWidth) * imageWidth
        val cy = clampCenter(centerY, srcHeight / imageHeight) * imageHeight
        return RectF(cx - srcWidth / 2f, cy - srcHeight / 2f, cx + srcWidth / 2f, cy + srcHeight / 2f)
    }

    /** Keeps a window of [windowFraction] fully inside 0..1. */
    fun clampCenter(center: Float, windowFraction: Float): Float {
        val half = (windowFraction / 2f).coerceAtMost(0.5f)
        return center.coerceIn(half, 1f - half)
    }
}
