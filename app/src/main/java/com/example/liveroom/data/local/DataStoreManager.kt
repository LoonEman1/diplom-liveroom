package com.example.liveroom.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs_live_room")

object DataStoreManager {

    suspend fun saveToken(context : Context, token : String, tokenKey : String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey(tokenKey)] = token
        }
    }

    fun getToken(context : Context, tokenKey : String) : Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(tokenKey)]
        }
    }

    suspend fun saveRememberMeState(context : Context, state : Boolean) {
        context.dataStore.edit { preferences ->
            preferences[booleanPreferencesKey("remember_me")] = state
        }
    }

    suspend fun saveInt(context: Context, int : Int, key : String) {
        context.dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = int
        }
    }

    fun getRememberMeState(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey("remember_me")] ?: false
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

    fun getInt(context: Context, key: String) : Flow<Int?>  {
        return context.dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)]
        }
    }
}