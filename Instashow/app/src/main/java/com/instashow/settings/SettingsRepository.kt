package com.instashow.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

private val imperialKey = booleanPreferencesKey("imperial")
private val stepGoalKey = longPreferencesKey("step_goal")
private val sampleDataKey = booleanPreferencesKey("sample_data")
private val onboardedKey = booleanPreferencesKey("onboarded")

enum class Units {
    Metric,
    Imperial,
}

data class Settings(
    val units: Units = defaultUnits(),
    val stepGoal: Long = DEFAULT_STEP_GOAL,
    val sampleData: Boolean = false,
    val onboarded: Boolean = false,
)

const val DEFAULT_STEP_GOAL = 10_000L
const val MIN_STEP_GOAL = 3_000L
const val MAX_STEP_GOAL = 25_000L

/** US, Liberia and Myanmar measure distance in miles. Everyone else gets kilometers by default. */
fun defaultUnits(locale: Locale = Locale.getDefault()): Units =
    if (locale.country in setOf("US", "LR", "MM")) Units.Imperial else Units.Metric

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            units = prefs[imperialKey]?.let { if (it) Units.Imperial else Units.Metric } ?: defaultUnits(),
            stepGoal = (prefs[stepGoalKey] ?: DEFAULT_STEP_GOAL).coerceIn(MIN_STEP_GOAL, MAX_STEP_GOAL),
            sampleData = prefs[sampleDataKey] ?: false,
            onboarded = prefs[onboardedKey] ?: false,
        )
    }

    suspend fun setUnits(units: Units) {
        dataStore.edit { it[imperialKey] = units == Units.Imperial }
    }

    suspend fun setStepGoal(goal: Long) {
        dataStore.edit { it[stepGoalKey] = goal.coerceIn(MIN_STEP_GOAL, MAX_STEP_GOAL) }
    }

    suspend fun setSampleData(enabled: Boolean) {
        dataStore.edit { it[sampleDataKey] = enabled }
    }

    suspend fun setOnboarded(onboarded: Boolean) {
        dataStore.edit { it[onboardedKey] = onboarded }
    }
}
