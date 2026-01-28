package com.example.liveroom.data.model

import com.example.liveroom.data.remote.dto.Server

sealed class ServerEvent {
    data class ServerCreated(val server: Server) : ServerEvent()
    data class ServerEdited(val serverName: String) : ServerEvent()
    class ServerDeleted(val serverName : String) : ServerEvent()
    class TokenGenerated : ServerEvent()
    data class Error(val message: String) : ServerEvent()
    data class ValidationError(val message: String) : ServerEvent()
}