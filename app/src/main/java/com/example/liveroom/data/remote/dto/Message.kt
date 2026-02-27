package com.example.liveroom.data.remote.dto

data class Message(
    val id: Long,
    val conversationId: Long,
    val type: String,
    val systemEvent: String?,
    val systemPayload: Any?,
    val author: Author?,
    val createdAt: String,
    val editedAt: String?,
    val deletedAt: String?,
    val content: String?,
    val replyTo: ReplyTo?,
    val attachmentsCount: Int
)

data class UserShort(
    val id: Long,
    val username: String,
    val avatarUrl: String? = null
)

data class MessageReaction(
    val emoji: String,
    val count: Int,
    val reactedByMe: Boolean = false
)

data class SendMessageRequest(
    val content: String,
    val replyToMessageId: Long? = null
)

data class ReadUpdate(
    val conversationId: Long,
    val userId: Long,
    val lastReadMessageId: Long,
    val unreadCount: Int
)

data class Author(
    val userId: Int,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val avatarUpdatedAt: Long?
)

data class ReplyTo(
    val id: Long,
    val content: String?
)


data class EditMessageRequest(
    val content: String
)

