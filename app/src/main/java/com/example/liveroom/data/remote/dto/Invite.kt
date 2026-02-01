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
        val invitedByUsername: String,
        val expiresAt: String
    ) : Invite()

    fun getFormattedExpiresAt(time : String): String {
        return try {
            val instant = java.time.Instant.parse(time)
            val formatter = java.time.format.DateTimeFormatter
                .ofPattern("HH:mm, dd.MM")
                .withZone(java.time.ZoneId.systemDefault())
            instant.atZone(java.time.ZoneId.systemDefault()).format(formatter)
        } catch (e: Exception) {
            time
        }
    }
}

data class InviteUserRequest(
    val username: String
)

data class JoinByTokenRequest(
    val token : String
)
