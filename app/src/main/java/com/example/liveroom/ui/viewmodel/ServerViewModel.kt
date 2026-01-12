package com.example.liveroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.net.Uri
import android.util.Log
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.repository.ServerRepository
import javax.inject.Inject

@HiltViewModel
class ServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _servers = MutableStateFlow<List<Server>>(emptyList())
    val servers: StateFlow<List<Server>> = _servers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadServers(userId: Int, token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = serverRepository.getServers(userId, token)
            result.onSuccess { servers ->
                _servers.value = servers
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message
            }
            _isLoading.value = false
        }
    }

    fun createServer(
        name: String,
        imageUri: Uri?,
        userId: Int,
        token: String,
        onSuccess: (server: Server) -> Unit,
        onError: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val createResult = serverRepository.createServer(name, token)

                createResult.onSuccess { createdServer ->
                    Log.d("serverCreation", "Server created: ${createdServer.name}")
                    if (imageUri != null) {
                        val avatarResult = serverRepository.uploadServerAvatar(
                            createdServer.id,
                            createdServer.name,
                            imageUri,
                            token
                        )

                        avatarResult.onSuccess { serverWithAvatar ->
                            Log.d("serverCreation", "Avatar uploaded successfully")
                            _servers.value = _servers.value + serverWithAvatar
                            _error.value = null
                            onSuccess(serverWithAvatar)
                        }.onFailure { exception ->
                            Log.d("serverCreation", "Avatar upload failed ${exception.message}")
                            val errorMsg = exception.message ?: "Failed to upload avatar"
                            _error.value = errorMsg
                            onError(errorMsg)
                        }
                    } else {
                        Log.d("serverCreation", "No avatar")
                        _servers.value = _servers.value + createdServer
                        _error.value = null
                        onSuccess(createdServer)
                    }
                }.onFailure { exception ->
                    Log.d("serverCreation", "Server creation failed: ${exception.message}")
                    val errorMsg = exception.message ?: "Failed to create server"
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e: Exception) {
                Log.d("serverCreation", "Exception: ${e.message}")
                _error.value = e.message
                onError(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getServer(userId: Int?, token : String?) {
        if(userId == null || token == null) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = serverRepository.getServers(userId, token)
                result.onSuccess { servers ->
                    _servers.value = servers
                    _error.value = null
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            } catch(e : Exception) {
                _error.value = e.message
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}
