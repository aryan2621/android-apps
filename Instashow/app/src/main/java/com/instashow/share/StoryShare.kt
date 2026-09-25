package com.instashow.share

import android.app.Activity
import android.content.ClipData
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.instashow.BuildConfig
import com.instashow.R
import java.io.File
import java.io.FileOutputStream

class StoryShare(private val context: android.content.Context) {
    fun saveToGallery(bitmap: Bitmap): Uri {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Instashow-${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Instashow")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not save the story")
        try {
            resolver.openOutputStream(uri)?.use { output ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) { "Could not save the story" }
            } ?: error("Could not save the story")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }
        } catch (exception: Exception) {
            resolver.delete(uri, null, null)
            throw exception
        }
        return uri
    }

    fun share(activity: Activity, bitmap: Bitmap) {
        val uri = writePng(bitmap, "story")
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri("story", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(Intent.createChooser(send, activity.getString(R.string.share_chooser)))
    }

    fun isInstagramInstalled(): Boolean = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(INSTAGRAM, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(INSTAGRAM, 0)
        }
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }

    /** Opens Instagram's story composer with the finished story as the full-screen background. */
    fun shareToInstagramStory(activity: Activity, story: Bitmap) {
        val uri = writePng(story, "ig-story")
        val intent = Intent(ADD_TO_STORY).apply {
            setDataAndType(uri, "image/png")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(SOURCE_APPLICATION, sourceApplication())
            setPackage(INSTAGRAM)
        }
        activity.grantUriPermission(INSTAGRAM, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(intent)
    }

    /** Meta app id if set in local.properties, otherwise the package name. Sharing works without one. */
    private fun sourceApplication(): String = BuildConfig.FACEBOOK_APP_ID.ifBlank { context.packageName }

    private fun writePng(bitmap: Bitmap, prefix: String): Uri = write(bitmap, prefix, "png", Bitmap.CompressFormat.PNG, 100)

    private fun write(bitmap: Bitmap, prefix: String, extension: String, format: Bitmap.CompressFormat, quality: Int): Uri {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        dir.listFiles()
            ?.filter { it.name.startsWith("$prefix-") }
            ?.sortedByDescending { it.lastModified() }
            ?.drop(2)
            ?.forEach { it.delete() }
        val file = File(dir, "$prefix-${System.currentTimeMillis()}.$extension")
        FileOutputStream(file).use { output ->
            check(bitmap.compress(format, quality, output)) { "Could not write story image" }
        }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private companion object {
        const val INSTAGRAM = "com.instagram.android"
        const val ADD_TO_STORY = "com.instagram.share.ADD_TO_STORY"
        const val SOURCE_APPLICATION = "source_application"
    }
}
