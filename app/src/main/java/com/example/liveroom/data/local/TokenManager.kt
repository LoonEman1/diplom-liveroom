package com.example.liveroom.data.local

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext val context : Context
) {

    private val _accessToken = MutableStateFlow<String?>(null)

    init {
        runBlocking {
            val token = DataStoreManager.getToken(context, "access").first()
            _accessToken.value = token
        }
    }

    suspend fun saveAccessToken(token: String) {
        _accessToken.value = token
        DataStoreManager.saveToken(context, token, "access")
    }

    fun getAccessToken(): String? {
        if (_accessToken.value == null) {
            runBlocking {
                _accessToken.value = DataStoreManager.getToken(context, "access").first()
            }
        }
        return _accessToken.value
    }

    suspend fun clearToken() {
        _accessToken.value = null
        DataStoreManager.clearAllData(context)
    }
}
