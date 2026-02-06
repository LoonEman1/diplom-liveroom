package com.example.liveroom.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveroom.data.local.TokenManager
import com.example.liveroom.data.model.UserEvent
import com.example.liveroom.data.remote.dto.UserInfo
import com.example.liveroom.data.repository.TokenRepository
import com.example.liveroom.data.repository.UserRepository
import com.example.liveroom.util.getServerErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val tokenManager: TokenManager,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    private val _username = MutableStateFlow<String>("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _accessToken = MutableStateFlow<String>("")
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    private val _refreshToken = MutableStateFlow<String>("")
    val refreshToken: StateFlow<String> = _refreshToken.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _userEvents = MutableSharedFlow<UserEvent>()
    val userEvents: SharedFlow<UserEvent> = _userEvents.asSharedFlow()

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _accessToken.collect { token ->
                if (token.isNotEmpty()) {
                    tokenManager.saveAccessToken(token)
                }
                else if(token.isEmpty()) {
                    _accessToken.value = tokenManager.getAccessToken() ?: "null"
                }
            }
        }

    }

    fun setUserData(
        userId: Int,
        username: String,
        accessToken: String,
        refreshToken: String,
    ) {
        _userId.value = userId
        _username.value = username
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken

        if (userId != null && accessToken != null) {
            _isAuthenticated.value = true
        }
    }

    fun clearUserData() {
        _userId.value = null
        _username.value = ""
        _accessToken.value = ""
        _refreshToken.value = ""
        _isAuthenticated.value = false
    }

    fun getUserInfo() {
        viewModelScope.launch {
            _isLoading.value = true

            val result = withContext(Dispatchers.IO){
                userRepository.getUserInfo()
            }

            result.onSuccess { userInfo ->
                _userInfo.value = userInfo
                _username.value = userInfo.nickname
                _userId.value = userInfo.userId
                _userEvents.emit(UserEvent.UserLoaded)
            }.onFailure { throwable ->
                Log.e("UserViewModel", "Failed to load user: ${throwable.message}")
                _userEvents.emit(UserEvent.Error(throwable.getServerErrorMessage()))
            }

            _isLoading.value = false
        }
    }

}