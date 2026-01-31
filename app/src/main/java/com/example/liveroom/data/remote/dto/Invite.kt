package com.example.liveroom.data.remote.dto

sealed class Invite {
    data class TokenInvite(
        val inviteId: Int,
        val serverId: Int,
        val serverName: String,
        val type: String = "TOKEN",
        val token: String,
        val maxUses: Int?,
        val usedCount: Int,
        val expiresAt: String
    ) : Invite()

    data class UserInvite(
        val inviteId: Int,
        val serverId: Int,
        val serverName: String,
        val type: String = "USER",
        val invitedUserId: Int,
        val invitedUsername: String,
        val expiresAt: String
    ) : Invite()
}

data class InviteUserRequest(
    val username: String
)

data class JoinByTokenRequest(
    val token : String
)
