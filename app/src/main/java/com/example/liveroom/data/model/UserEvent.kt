package com.example.liveroom.data.model

sealed class UserEvent {
    object UserLoaded : UserEvent()
    object ProfileUpdated : UserEvent()
    data class Error(val message: String) : UserEvent()
    object UserLogOuted : UserEvent()
}