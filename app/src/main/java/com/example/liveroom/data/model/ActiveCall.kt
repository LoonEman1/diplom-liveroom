package com.example.liveroom.data.model
enum class CallSignalType {
    START, JOIN, LEAVE, END, OFFER, ANSWER, ICE, INVITE, BUSY, ERROR
}

enum class CallKind {
    AUDIO, VIDEO
}

data class CallClientMessage(
    val type: CallSignalType,
    val callId: String? = null,
    val kind: CallKind? = null,
    val inviteUserIds: List<Long>? = null,
    val toUserId: Long? = null,
    val sdp: String? = null,
    val ice: IceCandidateDto? = null
)

data class IceCandidateDto(
    val candidate: String,
    val sdpMid: String?,
    val sdpMLineIndex: Int
)

data class ActiveCall(
    val callId: String,
    val serverId: Long,
    val conversationId: Long,
    val kind: CallKind,
    val participants: Set<Long> = emptySet(),
    val startedAt: String,
    val duration: String = "00:00",
    val startedByUserId: Long
)

sealed class CallEvent {
    data class Started(val call: ActiveCall) : CallEvent()
    data class ParticipantJoined(val userId: Long) : CallEvent()
    data class ParticipantLeft(val userId: Long) : CallEvent()
    data class Ended(val callId: String, val reason: String?) : CallEvent()
    data class IncomingInvite(val callId: String, val fromUserId: Long) : CallEvent()
}

data class IncomingCall(
    val callId: String,
    val fromUserId: Long,
    val serverId: Long? = null,
    val conversationId: Long? = null
)

data class CallParticipantEvent(
    val callId: String,
    val userId: Long,
    val joined: Boolean
)

data class CallEndedEvent(
    val callId: String,
    val reason: String?
)