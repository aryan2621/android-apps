package com.example.coollauncher.data

/**
 * User-defined group/category of apps.
 */
data class AppGroup(
    val id: String,
    val name: String,
    val appPackageNames: List<String> = emptyList(),
)
