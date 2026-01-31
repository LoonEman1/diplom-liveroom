package com.example.liveroom.data.model

import android.content.Context
import com.example.liveroom.R
import com.example.liveroom.data.remote.dto.Server

sealed class ServerEvent {
    data class ServerCreated(val server: Server) : ServerEvent()
    data class ServerEdited(val serverName: String) : ServerEvent()
    data class ServerDeleted(val serverName : String) : ServerEvent()
    data object TokenGenerated : ServerEvent()
    data object UserJoined : ServerEvent()
    data class Error(val message: String) : ServerEvent()
    data class ValidationError(val error : ServerError) : ServerEvent()
    data class UserInvited(val username: String) : ServerEvent()
}


sealed class ServerError {
    object EmptyUsername : ServerError()
    object NoChangesToUpdate : ServerError()
    object TokenIsEmpty : ServerError()
}

fun getErrorMessage(context: Context, error: ServerError): String {
    return when (error) {
        ServerError.EmptyUsername -> context.getString(R.string.empty_username)
        ServerError.NoChangesToUpdate -> context.getString(R.string.no_changes_to_update)
        ServerError.TokenIsEmpty -> context.getString(R.string.token_is_empty)
    }
}