package com.example.liveroom.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.net.Uri
import android.util.Log
import com.example.liveroom.data.model.ServerEvent
import com.example.liveroom.data.remote.dto.Role
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.repository.ServerRepository
import kotlinx.coroutines.flow.asSharedFlow
import java.time.Instant
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

    private val _selectedServerId = MutableStateFlow<Int?>(null)
    val selectedServerId = _selectedServerId.asStateFlow()

    private val _serverEvents = MutableSharedFlow<ServerEvent>()
    val serverEvents = _serverEvents.asSharedFlow()

    private val _generatedToken = MutableStateFlow<String?>(null)
    val generatedToken = _generatedToken.asStateFlow()

    fun setSelectedServerId(selectedServerId: Int) {
        _selectedServerId.value = selectedServerId
    }

    fun createServer(
        name: String,
        imageUri: Uri?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val createResult = withContext(Dispatchers.IO) {
                    serverRepository.createServer(name, imageUri)
                }

                createResult.onSuccess { createdServer ->
                    val serverWithRole = createdServer.copy(
                        myRole = Role(
                            id = -1,
                            name = "OWNER",
                            power = 100,
                            canManageMembers = true,
                            canManageConversations = true
                        ),
                        createdAt = Instant.now().toString()
                    )

                    Log.d("ServerViewModel", "Server created: ${createdServer.name}")
                    _servers.value = _servers.value + serverWithRole
                    _selectedServerId.value = serverWithRole.id
                    _serverEvents.emit(ServerEvent.ServerCreated(serverWithRole))
                    onSuccess()
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Failed to create server"
                    _error.value = errorMsg
                    _serverEvents.emit(ServerEvent.Error(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception creating server: ${e.message}", e)
                val errorMsg = e.message ?: "Unknown error"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getServers(userId: Int?) {
        if (userId == null) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.getServers(userId)
                }

                result.onSuccess { servers ->
                    _servers.value = servers
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Failed to load servers"
                    _error.value = errorMsg
                    _serverEvents.emit(ServerEvent.Error(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception loading servers: ${e.message}", e)
                val errorMsg = e.message ?: "Failed to load servers"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun deleteServer(
        server: Server,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.deleteServer(server.id)
                }

                result.onSuccess {
                    _servers.value = _servers.value.filter { it.id != server.id }
                    if (_selectedServerId.value == server.id) {
                        _selectedServerId.value = _servers.value.firstOrNull()?.id
                    }

                    Log.d("ServerViewModel", "Server deleted: ${server.name}")
                    _serverEvents.emit(ServerEvent.ServerDeleted(server.name))
                    onSuccess()
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Failed to delete server"
                    Log.e("ServerViewModel", "Delete server error: $errorMsg", exception)
                    _error.value = errorMsg
                    _serverEvents.emit(ServerEvent.Error(errorMsg))
                    onError()
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception deleting server: ${e.message}", e)
                val errorMsg = e.message ?: "Failed to delete server"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
                onError()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun editServer(
        serverId: Int,
        name: String?,
        imageUri: String?,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                if (name.isNullOrBlank() && imageUri.isNullOrBlank()) {
                    val errorMsg = "No changes to update"
                    _error.value = errorMsg
                    _serverEvents.emit(ServerEvent.ValidationError(errorMsg))
                    _isLoading.value = false
                    return@launch
                }

                val result = withContext(Dispatchers.IO) {
                    serverRepository.updateServer(serverId, name, imageUri)
                }

                result.onSuccess { updatedServer ->
                    _servers.update { currentList ->
                        currentList.map { server ->
                            if (server.id == serverId) {
                                server.copy(
                                    name = updatedServer.name,
                                    avatarUrl = updatedServer.avatarUrl ?: server.avatarUrl
                                )
                            } else {
                                server
                            }
                        }
                    }

                    Log.i("ServerViewModel", "Server edited successfully: $serverId")
                    _serverEvents.emit(ServerEvent.ServerEdited(updatedServer.name))
                    onSuccess()
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Failed to update server"
                    Log.e("ServerViewModel", "Edit server error: $errorMsg", exception)
                    _error.value = errorMsg
                    _serverEvents.emit(ServerEvent.Error(errorMsg))
                    onError()
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception editing server: ${e.message}", e)
                val errorMsg = e.message ?: "Failed to update server"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
                onError()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createServerToken(
        serverId: Int,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.createServerToken(serverId)
                }

                result.onSuccess { token ->
                    _servers.update { servers ->
                        servers.map { server ->
                            if (server.id == serverId) {
                                server.copy(serverToken = token)
                            } else {
                                server
                            }
                        }
                    }
                    _generatedToken.value = token.token
                    _serverEvents.emit(ServerEvent.TokenGenerated())
                    onSuccess()
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Failed to create token"
                    _error.value = errorMsg
                    _serverEvents.emit(ServerEvent.Error(errorMsg))
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception creating token: ${e.message}", e)
                val errorMsg = e.message ?: "Unknown error"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
            } finally {
                _isLoading.value = false
            }
        }
    }
}
