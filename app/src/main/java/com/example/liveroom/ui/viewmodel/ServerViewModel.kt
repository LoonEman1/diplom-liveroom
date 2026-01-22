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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.liveroom.data.remote.dto.Role
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.repository.ServerRepository
import com.example.liveroom.di.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.sql.Time
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import java.time.Instant

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


    fun setSelectedServerId(selectedServerId : Int) {
        _selectedServerId.value = selectedServerId
    }

    fun createServer(
        name: String,
        imageUri: Uri?,
        onSuccess: (server: Server) -> Unit,
        onError: (message: String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val createResult = serverRepository.createServer(name, imageUri)

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

                    Log.d("serverCreation", "Server created: ${createdServer.name}")
                    _servers.value = _servers.value + serverWithRole
                    _selectedServerId.value = serverWithRole.id
                    _error.value = null

                    onSuccess(serverWithRole)
                }.onFailure { exception ->
                    val errorMsg = exception.message ?: "Failed to create server"
                    _error.value = errorMsg
                    onError(errorMsg)
                }
            } catch (e : Exception) {
                Log.d("ServerCreation", "Exception : ${e.message}")
                _error.value = e.message
                onError(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getServers(userId: Int?) {
        if(userId == null) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = serverRepository.getServers(userId)
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

    fun deleteServer(
        server: Server,
        onSuccess: () -> Unit,
        onError: (message: String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = serverRepository.deleteServer(server.id)
                result.onSuccess {
                    _servers.value = _servers.value.filter { it.id != server.id }
                    if(_selectedServerId.value == server.id) {
                        _selectedServerId.value = _servers.value.firstOrNull()?.id ?: -1
                    }
                    _error.value = null
                    Log.d("ServerDeletion", "Server deleted: ${server.name}")
                    onSuccess()
                }.onFailure { exception ->
                    val errorMessage = exception.message ?: "Failed to delete server"
                    _error.value = errorMessage
                    Log.e("ServerDeletion", "Error: ${errorMessage}")
                    onError(errorMessage)
                }
            } catch(e : Exception) {
                _error.value = e.message
                Log.e("ServerDeletion", "Exception: ${e.message}")
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
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            Log.d("ServerEdit", "Starting edit: serverId=$serverId, name=$name, imageUri=$imageUri")

            try {
                if (name.isNullOrBlank() && imageUri.isNullOrBlank()) {
                    val errorMsg = "No changes to update"
                    Log.w("ServerEdit", errorMsg)
                    _error.value = errorMsg
                    withContext(Dispatchers.Main) {
                    onError(errorMsg)
                    }
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ServerEdit", "Input validation passed")

                val result = serverRepository.updateServer(serverId, name, imageUri)

                result.onSuccess { updatedServer ->
                    Log.d("ServerEdit", "Repository call successful")

                    _servers.update { currentList ->
                        Log.d("ServerEdit", "Updating local state, current server count: ${currentList.size}")

                        currentList.map { server ->
                            if (server.id == serverId) {
                                Log.d("ServerEdit", "Found server to update: ${server.name}")

                                val newServer = server.copy(
                                    name = updatedServer.name,
                                    avatarUrl = updatedServer.avatarUrl ?: server.avatarUrl
                                )

                                Log.d("ServerEdit", "Server updated: old name=${server.name}, new name=${newServer.name}")
                                Log.d("ServerEdit", "Avatar URL: old=${server.avatarUrl}, new=${newServer.avatarUrl}")
                                newServer
                            } else {
                                server
                            }
                        }
                    }

                    Log.i("ServerEdit", "Edit completed successfully for server $serverId")
                    _error.value = null
                    withContext(Dispatchers.Main) {
                    onSuccess()
                    }
                }

                result.onFailure { exception ->
                    val errorMsg = exception.message ?: "Unknown error during server update"
                    Log.e("ServerEdit", "Repository error: $errorMsg", exception)
                    _error.value = errorMsg
                    withContext(Dispatchers.Main) {
                    onError(errorMsg)
                    }
                }

            } catch(e: Exception) {
                val errorMsg = e.message ?: "Unexpected error"
                Log.e("ServerEdit", "Exception in editServer: $errorMsg", e)
                _error.value = errorMsg
                withContext(Dispatchers.Main) {
                onError(errorMsg)
                }
            } finally {
                Log.d("ServerEdit", "Setting isLoading to false")
                _isLoading.value = false
            }
        }
    }


}
