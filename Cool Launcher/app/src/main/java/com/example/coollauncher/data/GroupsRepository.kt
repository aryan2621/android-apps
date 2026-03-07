package com.example.coollauncher.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "groups")

private val GROUPS_KEY = stringPreferencesKey("app_groups")

class GroupsRepository(private val context: Context) {

    val groupsFlow: Flow<List<AppGroup>> = context.dataStore.data.map { prefs ->
        val json = prefs[GROUPS_KEY] ?: return@map emptyList()
        parseGroupsJson(json)
    }

    suspend fun getGroups(): List<AppGroup> {
        val prefs = context.dataStore.data.first()
        val json = prefs[GROUPS_KEY] ?: return emptyList()
        return parseGroupsJson(json)
    }

    suspend fun saveGroups(groups: List<AppGroup>) {
        context.dataStore.edit { prefs ->
            prefs[GROUPS_KEY] = groupsToJson(groups)
        }
    }

    private fun groupsToJson(groups: List<AppGroup>): String {
        val arr = JSONArray()
        for (g in groups) {
            val obj = JSONObject().apply {
                put("id", g.id)
                put("name", g.name)
                put("packages", JSONArray(g.appPackageNames))
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseGroupsJson(json: String): List<AppGroup> {
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val obj = arr.getJSONObject(i)
                val packages = mutableListOf<String>()
                val pkgArr = obj.getJSONArray("packages")
                for (j in 0 until pkgArr.length()) {
                    packages.add(pkgArr.getString(j))
                }
                AppGroup(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    appPackageNames = packages
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
