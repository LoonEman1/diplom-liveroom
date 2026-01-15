package com.example.liveroom.data.repository

import android.content.Context
import com.example.liveroom.data.local.DataStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TokenRepository @Inject constructor(
    @ApplicationContext val context : Context
) {
    suspend fun saveToken(token : String, tokenKey : String) {
        DataStoreManager.saveToken(context, token, tokenKey)
    }

    suspend fun getToken(tokenKey: String) {
        DataStoreManager.getToken(context, tokenKey)
    }

    suspend fun clearToken() {
        DataStoreManager.clearAllData(context)
    }
}