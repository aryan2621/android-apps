package com.example.coollauncher.data

import android.graphics.drawable.Drawable

/**
 * Represents a launchable app discovered from the system.
 */
data class AppInfo(
    val packageName: String,
    val label: CharSequence,
    val icon: Drawable,
)
