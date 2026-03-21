package com.example.liveroom.data.remote.dto

data class SessionSummaryDto(
    val sessionId: Long,
    val callId: String,
    val kind: String,
    val status: String,
    val startedAt: String,
    val endedAt: String?,
    val durationSeconds: Long,
    val uniqueAttendees: Int,
    val peakConcurrent: Int,
    val totalMessages: Int,
    val totalReads: Int,
    val totalReactions: Int
)

data class SessionDetailDto(
    val sessionId: Long,
    val serverId: Int,
    val conversationId: Long,
    val conversationTitle: String,
    val callId: String,
    val kind: String,
    val status: String,
    val startedByUserId: Int,
    val startedAt: String,
    val endedAt: String?,
    val durationSeconds: Long,
    val uniqueAttendees: Int,
    val peakConcurrent: Int,
    val avgPresenceSeconds: Double,
    val medianPresenceSeconds: Double,
    val avgPresenceRatio: Double,
    val activeChatUsers: Int,
    val totalMessages: Int,
    val totalReads: Int,
    val totalReactions: Int,
    val users: List<SessionUserDto>,
    val timeline: List<TimelineBucketDto>
)

data class SessionUserDto(
    val userId: Int,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUpdatedAt: Long?,
    val presenceSeconds: Long,
    val presenceRatio: Double,
    val joinsCount: Int,
    val lateJoin: Boolean,
    val earlyLeave: Boolean,
    val completed: Boolean,
    val messagesCount: Int,
    val readsCount: Int,
    val reactionsCount: Int,
    val engagementScore: Int
)

data class TimelineBucketDto(
    val bucketStart: String,
    val bucketEnd: String,
    val activeUsers: Int,
    val messagesCount: Int,
    val readsCount: Int,
    val reactionsCount: Int
)

data class PeriodSummaryDto(
    val serverId: Int,
    val conversationId: Long,
    val from: String,
    val to: String,
    val sessionsCount: Int,
    val avgAttendance: Double,
    val avgDurationSeconds: Double,
    val avgPresenceRatio: Double,
    val uniqueAttendees: Int,
    val totalMessages: Int,
    val totalReads: Int,
    val totalReactions: Int,
    val sessions: List<SessionSummaryDto>
)

data class PeriodUserDto(
    val userId: Int,
    val username: String,
    val firstName: String?,
    val lastName: String?,
    val avatarUpdatedAt: Long?,
    val sessionsJoined: Int,
    val sessionsCompleted: Int,
    val lateJoins: Int,
    val earlyLeaves: Int,
    val totalPresenceSeconds: Long,
    val avgPresenceSeconds: Double,
    val avgPresenceRatio: Double,
    val totalMessages: Int,
    val totalReads: Int,
    val totalReactions: Int,
    val engagementScore: Int
)