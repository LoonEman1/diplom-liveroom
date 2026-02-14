package com.example.liveroom.data.remote.dto


data class Server(
    val id : Int,
    val name : String,
    val avatarUrl : String? = null,
    val myRole: Role,
    val createdAt: String,
    val serverToken : Invite.TokenInvite? = null
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
data class Conversation(
    val id: Long,
    val serverId: Long,
    val serverName: String,
    val title: String,
    val isPrivate: Boolean,
    val createdAt: String,
    val createdByUserId: Long,
    val canManage: Boolean,
    val lastReadMessageId: Long? = null,
    val unreadCount: Int = 0,
    val lastMessage: Any? = null
)

data class ServerMember(
    val id: Long,
    val userId: Long,
    val username: String,
    val role: Role
)

data class CreateConversationRequest(
    val title: String,
    val isPrivate: Boolean = false,
    val memberUserIds: List<Long> = emptyList()
)