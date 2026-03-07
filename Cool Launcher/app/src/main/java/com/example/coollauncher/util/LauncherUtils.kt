package com.example.coollauncher.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

private const val TAG = "CoolLauncher"

/**
 * Returns true if this app is the current default home launcher.
 * When no default is set, resolveActivity can return the system ResolverActivity
 * (package "android" or similar); we treat that as "not default" so we show the prompt.
 */
fun Context.isDefaultHomeLauncher(): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
    }
    @Suppress("DEPRECATION")
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
    val defaultPkg = resolveInfo?.activityInfo?.packageName
    // System resolver (e.g. "android") means no default set — show chooser
    val isSystemResolver = defaultPkg == null ||
        defaultPkg == "android" ||
        defaultPkg.contains("ResolverActivity", ignoreCase = true)
    val result = !isSystemResolver && defaultPkg == packageName
    Log.d(TAG, "isDefaultHomeLauncher: defaultPkg=$defaultPkg, thisPackage=$packageName, isSystemResolver=$isSystemResolver, result=$result")
    return result
}

/**
 * Opens the system "Choose home app" / "Set default launcher" dialog.
 * Do not use Intent.createChooser() — it hides the "Always" option.
 * Starting MAIN + CATEGORY_HOME directly shows the proper resolver with "Just once" / "Always".
 * When called from an Activity, we avoid NEW_TASK so Back from the chooser returns to our app.
 */
fun Context.openSetDefaultLauncherChooser() {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        // Only use NEW_TASK when we don't have an Activity (e.g. from Application).
        // Otherwise the chooser runs in a new task and Back sends the user to the other launcher.
        if (this@openSetDefaultLauncherChooser !is Activity) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    Log.d(TAG, "openSetDefaultLauncherChooser: starting MAIN+HOME intent")
    try {
        startActivity(intent)
        Log.d(TAG, "openSetDefaultLauncherChooser: startActivity succeeded")
    } catch (e: Exception) {
        Log.e(TAG, "openSetDefaultLauncherChooser: startActivity failed", e)
    }
}

/**
 * Opens the system Default apps settings screen.
 * User can tap "Home app" (or similar) to change the default launcher.
 * When called from an Activity, we avoid NEW_TASK so Back returns to our app.
 */
fun Context.openDefaultAppsSettings() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS).apply {
        if (this@openDefaultAppsSettings !is Activity) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    Log.d(TAG, "openDefaultAppsSettings: opening Default apps settings")
    try {
        startActivity(intent)
        Log.d(TAG, "openDefaultAppsSettings: startActivity succeeded")
    } catch (e: Exception) {
        Log.e(TAG, "openDefaultAppsSettings: startActivity failed", e)
    }
}

/**
 * Opens the system App info screen for this app (user can uninstall from there).
 * When called from an Activity, we avoid NEW_TASK so Back returns to our app.
 */
fun Context.openAppInfoForUninstall() {
    openAppInfo(packageName)
}

/**
 * Opens the system App info / App settings screen for the given app (by package name).
 * User can view app info, permissions, storage, and uninstall from there.
 * When called from an Activity, we avoid NEW_TASK so Back returns to our app.
 */
fun Context.openAppInfo(packageName: String) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:$packageName")
        if (this@openAppInfo !is Activity) {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
    try {
        startActivity(intent)
    } catch (e: Exception) {
        Log.e(TAG, "openAppInfo: startActivity failed for $packageName", e)
    }
}
