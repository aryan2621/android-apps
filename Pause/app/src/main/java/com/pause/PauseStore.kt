package com.pause

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.dataStore by preferencesDataStore(name = "pause")

enum class Reason(val key: String) {
    Bored("bored"),
    Message("message"),
    Purpose("purpose"),
}

/** Counts for one calendar day. They start again from zero every morning. */
data class Today(
    val stopped: Int = 0,
    val opens: Map<String, Int> = emptyMap(),
    val reasons: Map<Reason, Int> = emptyMap(),
)

data class PauseState(
    val guarded: Set<String> = emptySet(),
    val dailyLimit: Int = PauseStore.DEFAULT_DAILY_LIMIT,
    val today: Today = Today(),
    /** When each app's "Open anyway" grace period ends, in epoch milliseconds. */
    val graceUntil: Map<String, Long> = emptyMap(),
) {
    fun inGrace(packageName: String, now: Long = System.currentTimeMillis()): Boolean =
        (graceUntil[packageName] ?: 0L) > now
}

class PauseStore(context: Context) {
    private val dataStore = context.applicationContext.dataStore

    val state: Flow<PauseState> = dataStore.data.map { prefs ->
        val isToday = prefs[dateKey] == today()
        PauseState(
            guarded = prefs[guardedKey].orEmpty(),
            dailyLimit = prefs[limitKey] ?: DEFAULT_DAILY_LIMIT,
            today = if (isToday) readToday(prefs) else Today(),
            graceUntil = prefs.asMap().entries
                .filter { it.key.name.startsWith(GRACE_PREFIX) }
                .associate { it.key.name.removePrefix(GRACE_PREFIX) to (it.value as Long) },
        )
    }

    /** Guards the usual scroll apps the first time, if they're installed. */
    suspend fun ensureDefaults(installed: Set<String>) {
        dataStore.edit { prefs ->
            if (prefs[setUpKey] == true) return@edit
            prefs[guardedKey] = DEFAULT_GUARDED.intersect(installed)
            prefs[setUpKey] = true
        }
    }

    suspend fun setGuarded(packageName: String, guarded: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[guardedKey].orEmpty()
            prefs[guardedKey] = if (guarded) current + packageName else current - packageName
        }
    }

    suspend fun setDailyLimit(limit: Int) {
        dataStore.edit { it[limitKey] = limit.coerceIn(MIN_DAILY_LIMIT, MAX_DAILY_LIMIT) }
    }

    /** Counts one more open of [packageName] today and returns the new total. */
    suspend fun recordOpen(packageName: String): Int {
        var total = 0
        dataStore.edit { prefs ->
            startTodayIfNeeded(prefs)
            val key = intPreferencesKey(OPENS_PREFIX + packageName)
            total = (prefs[key] ?: 0) + 1
            prefs[key] = total
        }
        return total
    }

    suspend fun recordStopped() {
        dataStore.edit { prefs ->
            startTodayIfNeeded(prefs)
            prefs[stoppedKey] = (prefs[stoppedKey] ?: 0) + 1
        }
    }

    /** The user went ahead: remember why, and leave the app alone for a few minutes. */
    suspend fun recordOpenAnyway(packageName: String, reason: Reason) {
        dataStore.edit { prefs ->
            startTodayIfNeeded(prefs)
            val key = intPreferencesKey(REASON_PREFIX + reason.key)
            prefs[key] = (prefs[key] ?: 0) + 1
            prefs[longPreferencesKey(GRACE_PREFIX + packageName)] = System.currentTimeMillis() + GRACE_MILLIS
        }
    }

    private fun readToday(prefs: Preferences): Today {
        val all = prefs.asMap()
        return Today(
            stopped = prefs[stoppedKey] ?: 0,
            opens = all.entries
                .filter { it.key.name.startsWith(OPENS_PREFIX) }
                .associate { it.key.name.removePrefix(OPENS_PREFIX) to (it.value as Int) },
            reasons = Reason.entries.associateWith { prefs[intPreferencesKey(REASON_PREFIX + it.key)] ?: 0 },
        )
    }

    /** Clears yesterday's counts the first time anything is recorded on a new day. */
    private fun startTodayIfNeeded(prefs: MutablePreferences) {
        val date = today()
        if (prefs[dateKey] == date) return
        prefs.asMap().keys
            .filter { key ->
                key.name.startsWith(OPENS_PREFIX) || key.name.startsWith(REASON_PREFIX) ||
                    key.name.startsWith(GRACE_PREFIX) || key == stoppedKey
            }
            .forEach { prefs.remove(it) }
        prefs[dateKey] = date
    }

    private fun today(): String = LocalDate.now().toString()

    companion object {
        const val DEFAULT_DAILY_LIMIT = 5
        const val MIN_DAILY_LIMIT = 1
        const val MAX_DAILY_LIMIT = 30
        const val PAUSE_SECONDS = 5
        const val LONG_PAUSE_SECONDS = 15
        private const val GRACE_MILLIS = 5 * 60 * 1000L

        private const val OPENS_PREFIX = "opens:"
        private const val REASON_PREFIX = "reason:"
        private const val GRACE_PREFIX = "grace:"
        private val guardedKey = stringSetPreferencesKey("guarded")
        private val setUpKey = booleanPreferencesKey("set_up")
        private val limitKey = intPreferencesKey("daily_limit")
        private val dateKey = stringPreferencesKey("date")
        private val stoppedKey = intPreferencesKey("stopped")

        /** Feed and short-video apps. Messaging apps are left out on purpose. */
        val DEFAULT_GUARDED = setOf(
            "com.instagram.android",
            "com.google.android.youtube",
            "com.facebook.katana",
            "com.twitter.android",
            "com.snapchat.android",
            "com.reddit.frontpage",
            "com.zhiliaoapp.musically",
            "com.linkedin.android",
        )
    }
}
