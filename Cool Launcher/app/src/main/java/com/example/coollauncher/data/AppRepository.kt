package com.example.coollauncher.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.coollauncher.data.local.AppCache
import com.example.coollauncher.data.local.CachedAppEntry

class AppRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val ourPackage = context.packageName
    private val appCache = AppCache(context)

    /**
     * Loads apps from local cache and resolves icons from PackageManager.
     * Returns empty list if cache is empty.
     */
    suspend fun getCachedApps(): List<AppInfo> {
        val entries = appCache.getAll()
        return entries.mapNotNull { entry ->
            try {
                val icon: Drawable = try {
                    packageManager.getApplicationIcon(entry.packageName)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)!!
                }
                AppInfo(
                    packageName = entry.packageName,
                    label = entry.label,
                    icon = icon,
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Saves the given app list to the local cache (replaces existing).
     */
    suspend fun saveToCache(apps: List<AppInfo>) {
        val entries = apps.map { app ->
            CachedAppEntry(
                packageName = app.packageName,
                label = app.label.toString(),
            )
        }
        appCache.saveAll(entries)
    }

    /**
     * Returns all launchable apps, sorted by label.
     * Uses MAIN/LAUNCHER query first; on Android 11+ with QUERY_ALL_PACKAGES, also uses
     * getInstalledApplications + getLaunchIntentForPackage to pick up apps that some OEMs
     * hide from queryIntentActivities.
     */
    @Suppress("DEPRECATION")
    fun getLaunchableApps(): List<AppInfo> {
        val fromIntent = getLaunchableAppsFromIntentQuery()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val fromInstalled = getLaunchableAppsFromInstalled()
            val byPkg = fromIntent.associateBy { it.packageName }.toMutableMap()
            for (app in fromInstalled) {
                if (app.packageName !in byPkg) byPkg[app.packageName] = app
            }
            return byPkg.values.sortedBy { it.label.toString().lowercase() }
        }
        return fromIntent
    }

    private fun getLaunchableAppsFromIntentQuery(): List<AppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        var resolveInfoList = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (resolveInfoList.isEmpty()) {
            resolveInfoList = packageManager.queryIntentActivities(intent, 0)
        }
        return resolveInfoList
            .filter { it.activityInfo.packageName != ourPackage }
            .mapNotNull { resolveInfo ->
                try {
                    val pkg = resolveInfo.activityInfo.packageName
                    val label = resolveInfo.loadLabel(packageManager) ?: pkg
                    val icon: Drawable = try {
                        resolveInfo.loadIcon(packageManager)
                    } catch (e: Exception) {
                        ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)!!
                    }
                    AppInfo(packageName = pkg, label = label, icon = icon)
                } catch (e: Exception) {
                    null
                }
            }
            .distinctBy { it.packageName }
    }

    @Suppress("DEPRECATION")
    private fun getLaunchableAppsFromInstalled(): List<AppInfo> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return emptyList()
        val installed = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        return installed
            .filter { it.packageName != ourPackage }
            .filter { appInfo ->
                // Only include apps that would normally appear in an app drawer:
                // user-installed apps, or system apps that were updated (e.g. visible in drawer).
                // Exclude pre-installed system/internal apps that OEMs hide from the launcher.
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                !isSystem || isUpdatedSystem
            }
            .mapNotNull { appInfo ->
                if (packageManager.getLaunchIntentForPackage(appInfo.packageName) == null) return@mapNotNull null
                try {
                    val label = packageManager.getApplicationLabel(appInfo).toString()
                    val icon: Drawable = try {
                        appInfo.loadIcon(packageManager)
                    } catch (e: Exception) {
                        ContextCompat.getDrawable(context, android.R.drawable.sym_def_app_icon)!!
                    }
                    AppInfo(packageName = appInfo.packageName, label = label, icon = icon)
                } catch (e: Exception) {
                    null
                }
            }
            .distinctBy { it.packageName }
    }

    /**
     * Launches the app with the given package name. Returns true if launched, false otherwise.
     */
    fun launchApp(packageName: String): Boolean {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}
