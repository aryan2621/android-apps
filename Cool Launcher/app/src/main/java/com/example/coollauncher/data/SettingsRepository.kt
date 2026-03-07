package com.example.coollauncher.data

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "coollauncher_prefs"
private const val KEY_THEME_MODE = "theme_mode"    // 0=system, 1=light, 2=dark
private const val KEY_FONT_KEY = "font_key"        // 0=default, 1=serif, 2=monospace
private const val KEY_COLOR_THEME = "color_theme"  // 0=Teal, 1=Orange, 2=Red, 3=Green, 4=Yellow, 5=Blue, 6=Purple
private const val KEY_HARD_MINIMAL_MODE = "hard_minimal_mode"
private const val KEY_HAS_COMPLETED_TOUR = "has_completed_tour"

class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: Int
        get() = prefs.getInt(KEY_THEME_MODE, 0)
        set(value) = prefs.edit { putInt(KEY_THEME_MODE, value) }

    var fontKey: Int
        get() = prefs.getInt(KEY_FONT_KEY, 0)
        set(value) = prefs.edit { putInt(KEY_FONT_KEY, value) }

    var colorThemeKey: Int
        get() = prefs.getInt(KEY_COLOR_THEME, 0)
        set(value) = prefs.edit { putInt(KEY_COLOR_THEME, value) }

    var hardMinimalMode: Boolean
        get() = prefs.getBoolean(KEY_HARD_MINIMAL_MODE, false)
        set(value) = prefs.edit { putBoolean(KEY_HARD_MINIMAL_MODE, value) }

    var hasCompletedTour: Boolean
        get() = prefs.getBoolean(KEY_HAS_COMPLETED_TOUR, false)
        set(value) = prefs.edit { putBoolean(KEY_HAS_COMPLETED_TOUR, value) }

    /** Resets all launcher settings to defaults (theme, font, color, hard minimal mode, tour). */
    fun resetToDefaults() {
        prefs.edit {
            putInt(KEY_THEME_MODE, 0)
            putInt(KEY_FONT_KEY, 0)
            putInt(KEY_COLOR_THEME, 0)
            putBoolean(KEY_HARD_MINIMAL_MODE, false)
            putBoolean(KEY_HAS_COMPLETED_TOUR, false)
        }
    }
}
