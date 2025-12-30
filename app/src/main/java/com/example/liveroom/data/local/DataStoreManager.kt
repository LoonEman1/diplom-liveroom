package com.example.liveroom.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs_live_room")

object DataStoreManager {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")

    suspend fun saveToken(context : Context, token : String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    fun getToken(context : Context) : Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun clearAllData(context : Context) {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun logAllPreferences(context: Context) {
        context.dataStore.data.collect { preferences ->
            Log.d("DataStore", "=== All Preferences ===")
            preferences.asMap().forEach { (key, value) ->
                Log.d("DataStore", "$key = $value")
            }
            Log.d("DataStore", "=== End ===")
        }
    }
}