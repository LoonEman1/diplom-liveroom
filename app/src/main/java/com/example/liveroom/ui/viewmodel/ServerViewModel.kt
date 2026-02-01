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
import com.example.liveroom.data.model.ServerError
import com.example.liveroom.data.model.ServerEvent
import com.example.liveroom.data.remote.dto.ApiErrorResponse
import com.example.liveroom.data.remote.dto.Invite
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.repository.ServerRepository
import com.example.liveroom.util.getServerErrorMessage
import com.google.gson.Gson
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import retrofit2.HttpException
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

    private val _serverInvites = MutableStateFlow<List<Invite.UserInvite>>(emptyList())
    val serverInvites: StateFlow<List<Invite.UserInvite>> = _serverInvites.asStateFlow()

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
                    Log.d("ServerViewModel", "Server created: ${createdServer.name}")
                    _servers.value = _servers.value + createdServer
                    _selectedServerId.value = createdServer.id
                    _serverEvents.emit(ServerEvent.ServerCreated(createdServer))
                    onSuccess()
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
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
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
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
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
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
                    _serverEvents.emit(ServerEvent.ValidationError(ServerError.NoChangesToUpdate))
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
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
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
        serverId : Int,
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
                    _serverEvents.emit(ServerEvent.TokenGenerated)
                    onSuccess()
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
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

    fun inviteToServer(serverId :  Int, username : String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if(!username.isNullOrBlank()) {
                    val result = withContext(Dispatchers.IO) {
                        serverRepository.inviteUser(serverId, username)
                    }

                    result.onSuccess {
                        _serverEvents.emit(ServerEvent.UserInvited(username))
                    }

                    result.onFailure { exception ->
                        _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                    }
                } else
                    _serverEvents.emit(ServerEvent.ValidationError(ServerError.EmptyUsername))
            } catch(e : Exception) {
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun joinByToken(token : String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if(!token.isNullOrBlank()) {
                    val result = withContext(Dispatchers.IO) {
                        serverRepository.joinByToken(token)
                    }
                    result.onSuccess {
                        _serverEvents.emit(ServerEvent.UserJoined)
                    }
                    result.onFailure { exception ->
                        _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                    }
                } else _serverEvents.emit(ServerEvent.ValidationError(ServerError.TokenIsEmpty))
            } catch (e : Exception) {
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getInvites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.getInvites()
                }
                result.onSuccess { invites ->
                    _serverInvites.value = invites
                }.onFailure {
                    _serverEvents.emit(ServerEvent.Error(it.getServerErrorMessage()))
                }
            } catch (e : Exception) {
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Unknown Error"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptInvite(invite : Invite.UserInvite) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d("acceptInvite", "Starting with inviteId: ${invite.inviteId}")
            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.acceptInvite(invite.inviteId)
                }
                result.onSuccess {
                    Log.d("acceptInvite", "Success!")
                    _serverInvites.update { serversInvite ->
                        serversInvite.filter {
                            !(it.inviteId == invite.inviteId || it.serverId == invite.serverId)
                        }
                    }
                }.onFailure {
                    Log.e("acceptInvite", "Error: ${it.message}")
                    _serverEvents.emit(ServerEvent.Error(it.getServerErrorMessage()))
                }
            } catch (e : Exception) {
                Log.e("acceptInvite", "Exception: ${e.message}", e)
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Unknown Error"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun declineInvite(invite : Invite.UserInvite) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d("declineInvite", "Starting with inviteId: ${invite.inviteId}")
            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.declineInvite(invite.inviteId)
                }
                result.onSuccess {
                    Log.d("declineInvite", "Success!")
                    _serverInvites.update { serversInvite ->
                        serversInvite.filter {
                            !(it.inviteId == invite.inviteId || it.serverId == invite.serverId)
                        }
                    }
                }.onFailure {
                    Log.e("declineInvite", "Error: ${it.message}")
                    _serverEvents.emit(ServerEvent.Error(it.getServerErrorMessage()))
                }
            } catch (e : Exception) {
                Log.e("declineInvite", "Exception: ${e.message}", e)
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Unknown Error"))
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun leaveFromServer(server : Server) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            Log.d("leaveFromServer", "Starting leave from server with id: ${server.id}, name : ${server.name}")

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.leaveFromServer(server.id)
                }

                result.onSuccess {
                    _servers.update { serverList ->
                        serverList.filter {
                            (it.id != server.id)
                        }
                    }
                }.onFailure {
                    Log.e("leaveFromServer", "Error: ${it.message}")
                    _serverEvents.emit(ServerEvent.Error(it.getServerErrorMessage()))
                }
            } catch (e : Exception) {
                Log.e("leaveFromServer", "Exception: ${e.message}", e)
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Unknown Error"))
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun getServerToken(serverId: Int): StateFlow<String?> {
        return servers.map { serversList ->
                serversList.find { it.id == serverId }?.serverToken?.token
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = null
            )
    }

}
