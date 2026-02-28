package com.example.liveroom.data.remote.dto

data class UserInfo(
    val userId: Int,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val about: String?,
    val status: String,
    val lastSeen: String?,
    val createdAt: String,
    val hasAvatar: Boolean,
    val avatarUpdatedAt: Long,
    val showNicknamesOnServers: Boolean,
    val allowProfileView: Boolean,
    val avatarUrl: String?
)


data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val about: String? = null,
    val showNicknamesOnServers: Boolean? = null,
    val allowProfileView: Boolean? = null,
    val avatarUri: String? = null
)