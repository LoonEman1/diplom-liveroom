package com.example.liveroom.ui.viewmodel

import android.R.attr.type
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
import androidx.compose.runtime.mutableStateOf
import com.example.liveroom.data.local.WebSocketManager
import com.example.liveroom.data.model.ActiveCall
import com.example.liveroom.data.model.CallEndedEvent
import com.example.liveroom.data.model.CallKind
import com.example.liveroom.data.model.CallParticipantEvent
import com.example.liveroom.data.model.CallSignalType
import com.example.liveroom.data.model.ServerError
import com.example.liveroom.data.model.ServerEvent
import com.example.liveroom.data.remote.dto.ApiErrorResponse
import com.example.liveroom.data.remote.dto.Conversation
import com.example.liveroom.data.remote.dto.Invite
import com.example.liveroom.data.remote.dto.Message
import com.example.liveroom.data.remote.dto.SendMessageRequest
import com.example.liveroom.data.remote.dto.Server
import com.example.liveroom.data.remote.dto.ServerMember
import com.example.liveroom.data.repository.ServerRepository
import com.example.liveroom.data.webrtc.CallStateManager
import com.example.liveroom.util.getServerErrorMessage
import com.google.gson.Gson
import com.google.gson.Strictness
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import retrofit2.HttpException
import javax.inject.Inject
import com.example.liveroom.data.model.CallEvent
import com.example.liveroom.data.model.IncomingCall
import com.example.liveroom.data.model.IceCandidateDto
import com.example.liveroom.data.model.CallClientMessage
import com.example.liveroom.data.remote.dto.PeriodSummaryDto
import com.example.liveroom.data.remote.dto.PeriodUserDto
import com.example.liveroom.data.remote.dto.SessionDetailDto
import com.example.liveroom.data.remote.dto.SessionSummaryDto
import com.example.liveroom.data.repository.UserRepository
import com.example.liveroom.data.webrtc.WebRtcManager
import com.google.gson.JsonObject
import kotlinx.coroutines.async


@HiltViewModel
class ServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    private val webSocketManager: WebSocketManager,
    private val callStateManager: CallStateManager,
    private val webRtcManager: WebRtcManager,
    private val userRepository: UserRepository
) : ViewModel(), WebRtcManager.SignalingDelegate {

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

    private val _currentServerId = MutableStateFlow<Int?>(null)
    val currentServerId: StateFlow<Int?> = _currentServerId.asStateFlow()

    private val _serverInvites = MutableStateFlow<List<Invite.UserInvite>>(emptyList())
    val serverInvites: StateFlow<List<Invite.UserInvite>> = _serverInvites.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations = _conversations.asStateFlow()

    private val _members = MutableStateFlow<List<ServerMember>>(emptyList())
    val members = _members.asStateFlow()

    private val _selectedServer = MutableStateFlow<Server?>(null)
    val selectedServer = _selectedServer.asStateFlow()

    private val _currentConversationId = MutableStateFlow<Long?>(null)
    val currentConversationId: StateFlow<Long?> = _currentConversationId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private var currentWsType: String? = null

    val activeCall = callStateManager.activeCall
    val incomingCallDialog = callStateManager.incomingCallDialog

    private val _analyticsSessions = MutableStateFlow<List<SessionSummaryDto>>(emptyList())
    val analyticsSessions: StateFlow<List<SessionSummaryDto>> = _analyticsSessions.asStateFlow()

    private val _selectedSessionDetail = MutableStateFlow<SessionDetailDto?>(null)
    val selectedSessionDetail: StateFlow<SessionDetailDto?> = _selectedSessionDetail.asStateFlow()

    private val _periodSummary = MutableStateFlow<PeriodSummaryDto?>(null)
    val periodSummary: StateFlow<PeriodSummaryDto?> = _periodSummary.asStateFlow()

    private val _periodUsers = MutableStateFlow<List<PeriodUserDto>>(emptyList())
    val periodUsers: StateFlow<List<PeriodUserDto>> = _periodUsers.asStateFlow()

    private val _isAnalyticsLoading = MutableStateFlow(false)
    val isAnalyticsLoading: StateFlow<Boolean> = _isAnalyticsLoading.asStateFlow()

    private var myUserId: Long = -1


    init {
        webRtcManager.signalingDelegate = this


        viewModelScope.launch {
            userRepository.getUserInfo().onSuccess { info ->
                myUserId = info.userId.toLong()
            }
        }

    }

    fun clearAnalyticsData() {
        _analyticsSessions.value = emptyList()
        _selectedSessionDetail.value = null
        _periodSummary.value = null
        _periodUsers.value = emptyList()
    }



    private fun subscribeToCalls(serverId: Long, conversationId: Long, userId: Int) {
        Log.d("CallDebug", "📡 SUBSCRIBE calls: /topic/servers/$serverId/conversations/$conversationId/calls")
        webSocketManager.subscribe("/topic/servers/$serverId/conversations/$conversationId/calls")

        Log.d("CallDebug", "📡 SUBSCRIBE user: /topic/users/$userId/calls")
        webSocketManager.subscribe("/topic/users/$userId/calls")
    }

        fun setSelectedServerId(selectedServerId: Int) {
        _selectedServerId.value = selectedServerId
    }

    fun clearServerData() {
        _conversations.value = emptyList()
        _members.value = emptyList()
    }

    fun setSelectedServer(server : Server) {
        _selectedServer.value = server
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

    fun clear() {
        _servers.value = emptyList()
        _generatedToken.value = null
        _serverInvites.value = emptyList()
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

    fun inviteToServer(
        serverId :  Int, username : String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _error.value = null
            try {
                if(!username.isNullOrBlank()) {
                    val result = withContext(Dispatchers.IO) {
                        serverRepository.inviteUser(serverId, username)
                    }

                    result.onSuccess {
                        _serverEvents.emit(ServerEvent.UserInvited(username))
                        onSuccess()
                    }

                    result.onFailure { exception ->
                        _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                    }
                } else
                    _serverEvents.emit(ServerEvent.ValidationError(ServerError.EmptyUsername))
            } catch(e : Exception) {
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun joinByToken(
        token : String?,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if(!token.isNullOrBlank()) {
                    val result = withContext(Dispatchers.IO) {
                        serverRepository.joinByToken(token)
                    }
                    result.onSuccess { newServer ->
                        if (_servers.value.none { it.id == newServer.id }) {
                            _servers.value += newServer
                            _serverEvents.emit(ServerEvent.UserJoined)
                        } else {
                            _serverEvents.emit(ServerEvent.AlreadyJoined(newServer.name))
                        }
                        onSuccess()
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

    fun loadServerIfNeeded(serverId: Int) {
        if (_currentServerId.value != serverId) {
            _currentServerId.value = serverId
            loadServerData(serverId)
        }
    }


    fun loadServerData(serverId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.loadServerData(serverId)
                }

                result.onSuccess { (chats, members) ->
                    _conversations.value = chats
                    _members.value = members
                    Log.i("ServerViewModel", "Chats loaded: ${chats.size}, members: ${members.size}")
                }.onFailure { exception ->
                    Log.e("ServerViewModel", "Error load server data ${exception.message}")
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception loading server data: ${e.message}", e)
                val errorMsg = e.message ?: "Failed to load server data"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createConversation(
        serverId: Int,
        title: String,
        isPrivate: Boolean = false
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.createConversation(serverId, title, isPrivate)
                }

                result.onSuccess { newChat ->
                    _conversations.update { currentChats ->
                        (listOf(newChat) + currentChats)
                    }
                    Log.i("ServerViewModel", "Chat created $title")
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception creating conversation: ${e.message}", e)
                val errorMsg = e.message ?: "Failed to create conversation"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateConversation(serverId: Int, conversationId: Long, newTitle: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.updateConversationTitle(serverId, conversationId, newTitle)
                }

                result.onSuccess { updatedConversation ->
                    _conversations.update { currentChats ->
                        currentChats.map { convo ->
                            if (convo.id.toLong() == conversationId) {
                                convo.copy(title = newTitle)
                            } else {
                                convo
                            }
                        }
                    }
                    Log.i("ServerViewModel", "Chat updated: $conversationId -> $newTitle")
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception updating conversation: ${e.message}", e)
                val errorMsg = e.message ?: "Failed to update conversation"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteConversation(serverId: Int, conversationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = withContext(Dispatchers.IO) {
                    serverRepository.deleteConversation(serverId, conversationId)
                }

                result.onSuccess {
                    _conversations.update { currentChats ->
                        currentChats.filter { it.id.toLong() != conversationId }
                    }
                    Log.i("ServerViewModel", "Chat deleted $conversationId")
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Exception deleting conversation: ${e.message}", e)
                val errorMsg = e.message ?: "Failed to delete conversation"
                _error.value = errorMsg
                _serverEvents.emit(ServerEvent.Error(errorMsg))
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun loadMessages(conversationId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val serverId = selectedServer.value?.id?.toLong() ?: return@launch
                val result = withContext(Dispatchers.IO) {
                    serverRepository.getMessages(serverId, conversationId, limit = 50)
                }
                result.onSuccess { messages ->
                    _messages.value = messages.sortedBy { it.createdAt }
                    Log.d("ServerVM", "Loaded ${messages.size} messages")
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerVM", "Load messages error: ${e.message}")
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Load failed"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendMessage(conversationId: Long, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            _error.value = null
            try {
                val serverId = selectedServer.value?.id?.toLong() ?: return@launch
                val request = SendMessageRequest(content = content)
                val result = withContext(Dispatchers.IO) {
                    serverRepository.sendMessage(serverId, conversationId, request)
                }
                result.onSuccess { message ->
                    _messages.value = (_messages.value + message).sortedBy { it.createdAt }
                    Log.d("ServerVM", "Message sent: ${message.id}")
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerVM", "Send error: ${e.message}")
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Send failed"))
            }
        }

        val serverId = selectedServer.value?.id?.toLong() ?: return
        webSocketManager.send(
            "/app/servers/$serverId/conversations/$conversationId/messages",
            """{"content": "${content.replace("\"", "\\\"")}"}"""
        )
    }

    fun onWebSocketMessage(text: String, userId: Int) {
        Log.d("ServerVM", "📨 RAW: $text")

        // ✅ Извлеки только JSON payload после \n\n
        val jsonStart = text.indexOf('{')
        if (jsonStart == -1) {
            Log.w("ServerVM", "No JSON in message")
            return
        }

        val jsonString = text.substring(jsonStart)
        Log.d("ServerVM", "📄 JSON only: $jsonString")

        viewModelScope.launch(Dispatchers.Default) {
            try {
                val jsonStart = text.indexOf("\n\n") + 2
                val jsonPayload = if (jsonStart < text.length) {
                    text.substring(jsonStart).trim().trimEnd('\u0000')
                } else {
                    Log.w("ServerVM", "No JSON payload found: $text")
                    return@launch
                }

                Log.d("ServerVM", "Clean JSON: $jsonPayload")

                val reader = JsonReader(jsonPayload.reader())
                reader.setStrictness(Strictness.LENIENT)
                val json = Gson().fromJson(reader, Map::class.java) as Map<String, Any?>

                val type = json["type"] as? String ?: return@launch


                currentWsType = type

                handleCallEvents(json, type, userId)


                when (type) {
                    "message.created" -> json["payload"]?.let { payload ->
                        val payloadJson = Gson().toJson(payload)
                        val message = Gson().fromJson(payloadJson, Message::class.java)
                        _messages.value = (_messages.value + message).sortedBy { it.createdAt }
                    }
                    "message.updated" -> json["payload"]?.let { payload ->
                        val payloadJson = Gson().toJson(payload)
                        val message = Gson().fromJson(payloadJson, Message::class.java)
                        _messages.value = _messages.value
                            .map { if (it.id == message.id) message else it }
                            .sortedBy { it.createdAt }
                    }
                    "message.deleted" -> json["payload"]?.let { payload ->
                        val payloadJson = Gson().toJson(payload)
                        val message = Gson().fromJson(payloadJson, Message::class.java)
                        _messages.value = _messages.value.filter { it.id != message.id }
                    }
                }
            } catch (e: Exception) {
                Log.e("ServerVM", "WS parse error: $text", e)
            }
        }
    }



    fun setCurrentConversation(conversationId: Long, userId : Int) {
        webSocketManager.onMessageReceived = null

        _currentConversationId.value = conversationId
        loadMessages(conversationId)
        val serverId = selectedServer.value?.id?.toLong() ?: return
        webSocketManager.subscribe("/topic/servers/$serverId/conversations/$conversationId")

        subscribeToCalls(serverId, conversationId, userId)


        webSocketManager.onMessageReceived = { message ->
            Log.d("ServerVM", "✅ Get WS message: $message")
            onWebSocketMessage(message, userId)
        }
    }

    fun editMessage(conversationId: Long, messageId: Long, newContent: String) {
        if (newContent.isBlank()) return
        viewModelScope.launch {
            _error.value = null
            try {
                val serverId = selectedServer.value?.id?.toLong() ?: return@launch

                val result = withContext(Dispatchers.IO) {
                    serverRepository.editMessage(serverId, conversationId, messageId, newContent)
                }

                result.onSuccess { updatedMessage ->
                    // Мгновенно обновляем локальный список
                    _messages.update { currentMessages ->
                        currentMessages.map {
                            if (it.id == messageId) updatedMessage else it
                        }
                    }
                    Log.d("ServerVM", "Message edited: $messageId")
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerVM", "Edit error: ${e.message}")
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Edit failed"))
            }
        }
    }

    fun deleteMessage(conversationId: Long, messageId: Long) {
        viewModelScope.launch {
            _error.value = null
            try {
                val serverId = selectedServer.value?.id?.toLong() ?: return@launch

                val result = withContext(Dispatchers.IO) {
                    serverRepository.deleteMessage(serverId, conversationId, messageId)
                }

                result.onSuccess {
                    _messages.update { currentMessages ->
                        currentMessages.map {
                            if (it.id == messageId) {
                                it.copy(content = null, deletedAt = System.currentTimeMillis().toString())
                            } else {
                                it
                            }
                        }
                    }
                    Log.d("ServerVM", "Message deleted: $messageId")
                }.onFailure { exception ->
                    _serverEvents.emit(ServerEvent.Error(exception.getServerErrorMessage()))
                }
            } catch (e: Exception) {
                Log.e("ServerVM", "Delete error: ${e.message}")
                _serverEvents.emit(ServerEvent.Error(e.message ?: "Delete failed"))
            }
        }
    }

    fun inviteToConversation(serverId: Int, conversationId: Long, userId: Int) {
        viewModelScope.launch {
            val result = serverRepository.inviteToConversation(serverId, conversationId, userId)
            result.onSuccess {
                Log.d("ChatDebug", "User $userId invited to conv $conversationId")
            }.onFailure {
                Log.e("ChatDebug", "Failed to invite: ${it.message}")
            }
        }
    }

    private suspend fun handleCallEvents(json: Map<String, Any?>, type: String, userId: Int) {
        val payload = json["payload"] ?: return

        when (type) {
            "call.started" -> {
                val call = parseCallStart(payload as Map<*, *>)
                callStateManager.updateActiveCall(call)

                if (call.startedByUserId == myUserId) {
                    Log.d("CallDebug", "👑 I started the call → auto JOIN")
                    joinCall(call.callId)
                } else {
                    Log.d("CallDebug", "👀 Call started by ${call.startedByUserId}, waiting for manual join")
                }
            }
            "call.participant.joined" -> {
                val event = parseParticipant(payload as Map<*, *>)
                callStateManager.participantJoined(event.callId, event.userId)

                val myUserId = userId.toLong()
                val activeCall = callStateManager.activeCall.value ?: return

                if (event.userId != myUserId) {
                    // Инициализирует только тот, у кого ID больше (или меньше, главное — консистентность)
                    if (myUserId < event.userId) {
                        Log.d("WebRTC", "I am the initiator, sending OFFER to ${event.userId}")
                        webRtcManager.createOffer(
                            callId = activeCall.callId,
                            remoteUserId = event.userId,
                            kind = activeCall.kind
                        )
                    } else {
                        Log.d("WebRTC", "I will wait for OFFER from ${event.userId}")
                    }
                }
            }
            "call.ended" -> {
                Log.d("CallDebug", "📴 CALL ENDED")
                val ended = parseCallEnded(payload as Map<*, *>)
                callStateManager.endCall(ended.callId)
                webRtcManager.closeCall(ended.callId)
            }
            "call.signal" -> {
                handleCallSignal(payload as Map<*, *>)
            }
            "call.participant.left" -> {
                val event = parseParticipant(payload as Map<*, *>)

                callStateManager.participantLeft(event.callId, event.userId)

                val activeCall = callStateManager.activeCall.value ?: return

                val remaining = callStateManager.getParticipantsCount(event.callId)

                Log.d("CallDebug", "👤 User ${event.userId} left, remaining=$remaining")

                if (remaining <= 1) {
                    Log.d("CallDebug", "⚠️ Last participant → ending locally")

                    callStateManager.endCall(event.callId, "last participant left")
                    webRtcManager.closeCall(event.callId)
                }
            }
        }
    }

    private fun parseCallStart(payload: Map<*, *>): ActiveCall {
        return ActiveCall(
            callId = payload["callId"] as String,
            serverId = (payload["serverId"] as Number).toLong(),
            conversationId = (payload["conversationId"] as Number).toLong(),
            kind = CallKind.valueOf(payload["kind"] as String),
            participants = emptySet(),
            startedAt = payload["startedAt"] as String,
            startedByUserId = (payload["startedByUserId"] as Number).toLong()
        )
    }

    private fun parseParticipant(payload: Map<*, *>): CallParticipantEvent {
        return CallParticipantEvent(
            callId = payload["callId"] as String,
            userId = (payload["userId"] as Number).toLong(),
            joined = currentWsType == "call.participant.joined"
        )
    }

    private fun parseCallEnded(payload: Map<*, *>): CallEndedEvent {
        return CallEndedEvent(
            callId = payload["callId"] as String,
            reason = payload["reason"] as? String
        )
    }

    private fun handleCallSignal(payload: Map<*, *>) {
        val signalStr = payload["signal"] as? String ?: run {
            Log.w("CallSignal", "No signal in payload")
            return
        }

        val signal = try {
            CallSignalType.valueOf(signalStr.uppercase())
        } catch (e: IllegalArgumentException) {
            Log.w("CallSignal", "Unknown signal: $signalStr")
            return
        }

        Log.d("CallSignal", "🔄 Processing $signal from ${payload["fromUserId"]}")

        when (signal) {
            CallSignalType.START -> {
                val callId = payload["callId"] as? String ?: return
                Log.d("CallSignal", "✅ START confirmed: $callId")
            }

            CallSignalType.INVITE -> {
                val callId = payload["callId"] as? String ?: return
                val fromUserId = (payload["fromUserId"] as? Number)?.toLong() ?: return
                val serverId = (payload["serverId"] as? Number)?.toLong()
                val conversationId = (payload["conversationId"] as? Number)?.toLong()

                Log.d("CallSignal", "📞 Incoming INVITE from $fromUserId")

                callStateManager.showIncomingCallDialog(
                    IncomingCall(
                        callId = callId,
                        fromUserId = fromUserId,
                        serverId = serverId,
                        conversationId = conversationId
                    )
                )
            }

            CallSignalType.JOIN -> {
                val callId = payload["callId"] as? String ?: return
                val userId = (payload["fromUserId"] as? Number)?.toLong() ?: return

                Log.d("CallSignal", "✅ User $userId JOINED $callId")
                callStateManager.participantJoined(callId, userId)
            }

            CallSignalType.LEAVE -> {
                val callId = payload["callId"] as? String ?: return
                val userIdLeft = (payload["fromUserId"] as? Number)?.toLong() ?: return
                val myUserId = // достань свой ID (из репозитория или константы)

                    Log.d("CallSignal", "👋 User $userIdLeft LEFT $callId")

                // 1. Обновляем UI (удаляем человека из списка)
                callStateManager.participantLeft(callId, userIdLeft)

                if (userIdLeft == myUserId.toLong()) {
                    // Я ВЫШЕЛ: Закрываем всё
                    Log.d("CallSignal", "🛑 I left the call. Cleaning up everything.")
                    webRtcManager.closeCall(callId)
                    // Хедер скроется, так как participantLeft занулит activeCall для меня
                } else {
                    // КТО-ТО ВЫШЕЛ: Закрываем поток только с ним
                    Log.d("CallSignal", "✂️ Closing connection with user $userIdLeft")
                    webRtcManager.closeConnectionForUser(userIdLeft)

                    // Проверяем, остался ли кто-то еще кроме меня
                    val currentCall = callStateManager.activeCall.value
                    // Если в списке остался 0 человек или только я один (зависит от того, хранишь ли ты себя в списке)
                    if (currentCall == null || currentCall.participants.isEmpty()) {
                        Log.d("CallSignal", "📉 No one left. Ending call session.")
                        webRtcManager.closeCall(callId)
                        callStateManager.endCall(callId)
                    }
                }
            }

            CallSignalType.END -> {
                val callId = payload["callId"] as? String ?: return
                Log.d("CallSignal", "🔚 END $callId")
                callStateManager.endCall(callId)
                webRtcManager.closeCall(callId)
            }

            CallSignalType.OFFER -> {
                val callId = payload["callId"] as? String ?: return
                val fromUserId = (payload["fromUserId"] as? Number)?.toLong() ?: return

                callStateManager.participantJoined(callId, fromUserId)

                val sdp = payload["sdp"] as? String ?: return
                val kind = (payload["kind"] as? String)
                    ?.let {
                        try {
                            CallKind.valueOf(it.uppercase())
                        } catch (e: Exception) {
                            CallKind.AUDIO
                        }
                    } ?: CallKind.AUDIO

                Log.d("WebRTC", "📥 OFFER for $callId from $fromUserId")
                webRtcManager.onRemoteOffer(
                    callId = callId,
                    fromUserId = fromUserId,
                    sdp = sdp,
                    kind = kind
                )
            }

            CallSignalType.ANSWER -> {
                val callId = payload["callId"] as? String ?: return
                val sdp = payload["sdp"] as? String ?: return

                Log.d("WebRTC", "📥 ANSWER for $callId")
                webRtcManager.onRemoteAnswer(
                    callId = callId,
                    sdp = sdp
                )
            }

            CallSignalType.ICE -> {
                val callId = payload["callId"] as? String ?: return
                val icePayload = payload["ice"] ?: run {
                    Log.w("WebRTC", "ICE payload is null")
                    return
                }

                val ice = try {
                    Gson().fromJson(
                        Gson().toJson(icePayload),
                        IceCandidateDto::class.java
                    )
                } catch (e: Exception) {
                    Log.e("WebRTC", "Failed to parse ICE payload", e)
                    return
                }

                Log.d("WebRTC", "🧊 ICE for $callId")
                webRtcManager.onRemoteIce(
                    callId = callId,
                    ice = ice
                )
            }

            CallSignalType.BUSY -> {
                val callId = payload["callId"] as? String
                Log.w("CallSignal", "❌ BUSY for callId=$callId")
            }

            CallSignalType.ERROR -> {
                val error = payload["error"] as? String
                val callId = payload["callId"] as? String
                Log.e("CallSignal", "💥 ERROR for callId=$callId : $error")
            }
        }
    }


    fun startCall(serverId: Long, conversationId: Long, kind: CallKind = CallKind.AUDIO) {
        val message = CallClientMessage(
            type = CallSignalType.START,
            kind = kind,
            inviteUserIds = emptyList()
        )

        webSocketManager.send(
            "/app/calls/servers/$serverId/conversations/$conversationId",
            Gson().toJson(message)
        )
    }

    fun joinCall(callId: String) {
        val serverId = selectedServer.value?.id?.toLong() ?: return
        val conversationId = currentConversationId.value ?: return

        val message = CallClientMessage(
            type = CallSignalType.JOIN,
            callId = callId
        )

        webSocketManager.send(
            "/app/calls/servers/$serverId/conversations/$conversationId",
            Gson().toJson(message)
        )
    }

    fun leaveCall(callId: String) {
        val serverId = selectedServer.value?.id?.toLong() ?: return
        val conversationId = currentConversationId.value ?: return

        val message = CallClientMessage(
            type = CallSignalType.LEAVE,
            callId = callId
        )

        webSocketManager.send(
            "/app/calls/servers/$serverId/conversations/$conversationId",
            Gson().toJson(message)
        )
    }

    fun endCallWs(callId: String) {
        val serverId = selectedServer.value?.id?.toLong() ?: return
        val conversationId = currentConversationId.value ?: return

        val message = CallClientMessage(
            type = CallSignalType.END,
            callId = callId
        )

        webSocketManager.send(
            "/app/calls/servers/$serverId/conversations/$conversationId",
            Gson().toJson(message)
        )
    }

    override fun sendOffer(callId: String, toUserId: Long, sdp: String) {
        val serverId = selectedServer.value?.id?.toLong() ?: return
        val conversationId = currentConversationId.value ?: return

        val msg = CallClientMessage(
            type = CallSignalType.OFFER,
            callId = callId,
            toUserId = toUserId,
            sdp = sdp
        )

        webSocketManager.send(
            "/app/calls/servers/$serverId/conversations/$conversationId",
            Gson().toJson(msg)
        )
    }

    override fun sendAnswer(callId: String, toUserId: Long, sdp: String) {
        val serverId = selectedServer.value?.id?.toLong() ?: return
        val conversationId = currentConversationId.value ?: return

        val msg = CallClientMessage(
            type = CallSignalType.ANSWER,
            callId = callId,
            toUserId = toUserId,
            sdp = sdp
        )

        webSocketManager.send(
            "/app/calls/servers/$serverId/conversations/$conversationId",
            Gson().toJson(msg)
        )
    }

    override fun sendIceCandidate(callId: String, toUserId: Long, ice: IceCandidateDto) {
        val serverId = selectedServer.value?.id?.toLong() ?: return
        val conversationId = currentConversationId.value ?: return

        val msg = CallClientMessage(
            type = CallSignalType.ICE,
            callId = callId,
            toUserId = toUserId,
            ice = ice
        )

        webSocketManager.send(
            "/app/calls/servers/$serverId/conversations/$conversationId",
            Gson().toJson(msg)
        )
    }

    fun loadAnalyticsSessions(serverId: Int, conversationId: Long) {
        viewModelScope.launch {
            _isAnalyticsLoading.value = true
            _error.value = null
            val result = withContext(Dispatchers.IO) {
                serverRepository.getAnalyticsSessions(serverId, conversationId)
            }
            result.onSuccess { sessions ->
                _analyticsSessions.value = sessions
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load sessions"
            }
            _isAnalyticsLoading.value = false
        }
    }

    fun loadSessionDetails(serverId: Int, conversationId: Long, sessionId: Long) {
        viewModelScope.launch {
            _isAnalyticsLoading.value = true
            _error.value = null
            val result = withContext(Dispatchers.IO) {
                serverRepository.getAnalyticsSessionDetails(serverId, conversationId, sessionId)
            }
            result.onSuccess { details ->
                _selectedSessionDetail.value = details
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load session details"
            }
            _isAnalyticsLoading.value = false
        }
    }

    fun loadPeriodAnalytics(serverId: Int, conversationId: Long, from: String, to: String) {
        viewModelScope.launch {
            _isAnalyticsLoading.value = true
            _error.value = null

            try {
                val summaryDeferred = async {
                    serverRepository.getAnalyticsPeriodSummary(serverId, conversationId, from, to)
                }
                val usersDeferred = async {
                    serverRepository.getAnalyticsPeriodUsers(serverId, conversationId, from, to)
                }

                val summaryResult = summaryDeferred.await()
                val usersResult = usersDeferred.await()

                if (summaryResult.isSuccess && usersResult.isSuccess) {
                    val summary = summaryResult.getOrNull()
                    _periodSummary.value = summary
                    _periodUsers.value = usersResult.getOrNull() ?: emptyList()
                    _analyticsSessions.value = summary?.sessions ?: emptyList()
                } else {
                    val summaryError = summaryResult.exceptionOrNull()?.message
                    val usersError = usersResult.exceptionOrNull()?.message
                    _error.value = summaryError ?: usersError ?: "Unknown analytics error"
                }
            } catch (e: Exception) {
                Log.e("ServerViewModel", "Period analytics error: ${e.message}")
                _error.value = e.message ?: "Failed to load period analytics"
            } finally {
                _isAnalyticsLoading.value = false
            }
        }
    }
}

