package com.example.liveroom.data.local

import android.content.Context
import android.provider.ContactsContract.Data
import android.util.Log
import com.example.liveroom.data.model.UserData
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
    private val _rememberMe = MutableStateFlow<Boolean?>(null)
    private val _refreshToken = MutableStateFlow<String?>(null)


    init {
        runBlocking {
            val token = DataStoreManager.getToken(context, "access").first()
            _accessToken.value = token
            val rememberMe = DataStoreManager.getRememberMeState(context).first()
            _rememberMe.value = rememberMe
        }
    }

    suspend fun saveAccessToken(token: String) {
        _accessToken.value = token
        DataStoreManager.saveToken(context, token, "access")
    }

    suspend fun saveRefreshToken(token: String) {
        _refreshToken.value = token
        DataStoreManager.saveToken(context, token, "refresh")
    }

    fun getRefreshToken(): String? {
        if (_refreshToken.value == null) {
            runBlocking {
                _refreshToken.value = DataStoreManager.getToken(context, "refresh").first()
            }
        }
        return _refreshToken.value
    }


    fun getAccessToken(): String? {
        if (_accessToken.value == null) {
            runBlocking {
                _accessToken.value = DataStoreManager.getToken(context, "access").first()
            }
        }
        return _accessToken.value
    }

    fun getRememberMe() : Boolean {
        if (_rememberMe.value == null) {
            runBlocking {
                _rememberMe.value = DataStoreManager.getRememberMeState(context).first()
            }
        }
        return _rememberMe.value!!
    }

    suspend fun setRememberMe(state : Boolean) {
        _rememberMe.value = state
        DataStoreManager.saveRememberMeState(context, state)
    }

    suspend fun saveUserData(
        userId : Int,
        nickname : String,
        accessToken : String,
        refreshToken : String,
        rememberMe : Boolean
    ) {
        DataStoreManager.saveToken(context, nickname, "nickname")
        DataStoreManager.saveToken(context, refreshToken, "refresh")
        saveAccessToken(accessToken)
        setRememberMe(rememberMe)
        DataStoreManager.saveInt(context, userId, "user_id")
    }

    suspend fun getUserData(): UserData? {
        return try {
            val userId = DataStoreManager.getInt(context, "user_id").first()
            val nickname = DataStoreManager.getToken(context, "nickname").first()
            val refreshToken = DataStoreManager.getToken(context, "refresh").first()
            val accessToken = getAccessToken()
            val rememberMe = DataStoreManager.getRememberMeState(context).first()

            if (userId != null && nickname != null && accessToken != null) {
                UserData(
                    userId = userId,
                    nickname = nickname,
                    accessToken = accessToken,
                    refreshToken = refreshToken ?: "",
                    rememberMe = rememberMe
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error loading user data", e)
            null
        }
    }

    suspend fun clearToken() {
        _accessToken.value = null
        _refreshToken.value = null
        _rememberMe.value = false
        DataStoreManager.clearAllData(context)
    }
}
