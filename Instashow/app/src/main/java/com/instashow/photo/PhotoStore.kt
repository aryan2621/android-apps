package com.instashow.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

class PhotoStore(private val context: Context) {
    fun newCaptureTarget(): Uri {
        val dir = File(context.cacheDir, "capture").apply { mkdirs() }
        val file = File(dir, "shot.jpg")
        if (file.exists()) file.delete()
        check(file.createNewFile()) { "Could not create a photo file" }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun import(source: Uri): File {
        val dir = File(context.cacheDir, "story").apply { mkdirs() }
        val staged = File(dir, "incoming")
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                FileOutputStream(staged).use { output -> input.copyTo(output) }
            } ?: error("Could not read that photo")
            if (staged.length() == 0L) error("Could not read that photo")

            val bitmap = decode(staged)
            try {
                val file = File(dir, "photo.jpg")
                FileOutputStream(file).use { output ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)) {
                        "Could not save that photo"
                    }
                }
                return file
            } finally {
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        } finally {
            staged.delete()
        }
    }

    fun load(file: File): Bitmap =
        BitmapFactory.decodeFile(file.absolutePath) ?: error("Could not read that photo")

    private fun decode(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth > 0 && bounds.outHeight > 0) {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_EDGE)
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val decoded = BitmapFactory.decodeFile(file.absolutePath, options)
            if (decoded != null) return scaleDown(applyExif(decoded, file), MAX_EDGE)
        }
        return decodeWithImageDecoder(file) ?: error("Could not read that photo")
    }

    private fun decodeWithImageDecoder(file: File): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return null
        return runCatching {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(file)) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = max(info.size.width, info.size.height)
                if (longest > MAX_EDGE) {
                    val scale = MAX_EDGE.toFloat() / longest
                    decoder.setTargetSize(
                        (info.size.width * scale).toInt().coerceAtLeast(1),
                        (info.size.height * scale).toInt().coerceAtLeast(1),
                    )
                }
            }
        }.getOrNull()
    }

    private fun applyExif(bitmap: Bitmap, file: File): Bitmap {
        val orientation = runCatching {
            ExifInterface(file).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL,
            )
        }.getOrNull()
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> return bitmap
        }
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotated = runCatching {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }.getOrElse { return bitmap }
        if (rotated !== bitmap && !bitmap.isRecycled) bitmap.recycle()
        return rotated
    }

    private fun sampleSize(width: Int, height: Int, maxEdge: Int): Int {
        if (width <= 0 || height <= 0) return 1
        var sample = 1
        val longest = max(width, height)
        while (longest / sample > maxEdge * 2) sample *= 2
        return sample
    }

    private fun scaleDown(bitmap: Bitmap, maxEdge: Int): Bitmap {
        val longest = max(bitmap.width, bitmap.height)
        if (longest <= maxEdge) return bitmap
        val scale = maxEdge.toFloat() / longest
        val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).toInt().coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
        if (scaled !== bitmap && !bitmap.isRecycled) bitmap.recycle()
        return scaled
    }

    private companion object {
        const val MAX_EDGE = 2160
        const val JPEG_QUALITY = 90
    }
}
