package com.example.coollauncher.data.local

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * File-based cache for app list (packageName + label). No Room/KSP/kapt required.
 */
class AppCache(private val context: Context) {

    private val cacheFile: File
        get() = File(context.applicationContext.filesDir, "cached_apps.json")

    suspend fun getAll(): List<CachedAppEntry> = withContext(Dispatchers.IO) {
        try {
            if (!cacheFile.exists()) return@withContext emptyList()
            val json = cacheFile.readText()
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                CachedAppEntry(
                    packageName = obj.getString("p"),
                    label = obj.getString("l"),
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveAll(entries: List<CachedAppEntry>) = withContext(Dispatchers.IO) {
        val arr = JSONArray()
        for (e in entries) {
            arr.put(JSONObject().apply {
                put("p", e.packageName)
                put("l", e.label)
            })
        }
        cacheFile.writeText(arr.toString())
    }
}

data class CachedAppEntry(
    val packageName: String,
    val label: String,
)
