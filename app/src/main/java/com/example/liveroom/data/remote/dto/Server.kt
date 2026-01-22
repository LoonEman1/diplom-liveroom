package com.example.liveroom.data.remote.dto


data class Server(
    val id : Int,
    val name : String,
    val avatarUrl : String? = null,
    val myRole: Role,
    val createdAt: String
)

data class Role(
    val id: Int,
    val name: String,
    val power: Int,
    val canManageMembers: Boolean,
    val canManageConversations: Boolean
)

data class CreateServerRequest(
    val name: String
)

data class UpdateServerRequest(
    val name: String?
)