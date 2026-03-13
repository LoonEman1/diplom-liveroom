package com.example.liveroom.data.webrtc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liveroom.data.local.WebSocketManager
import com.example.liveroom.data.model.ActiveCall
import com.example.liveroom.data.model.CallEvent
import com.example.liveroom.data.model.IncomingCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CallStateManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    private val _callEvents = MutableSharedFlow<CallEvent>()
    val callEvents = _callEvents.asSharedFlow()

    private val _incomingCallDialog = MutableStateFlow<IncomingCall?>(null)
    val incomingCallDialog = _incomingCallDialog.asStateFlow()

    fun updateActiveCall(call: ActiveCall) {
        _activeCall.value = call
        scope.launch { _callEvents.emit(CallEvent.Started(call)) }
    }

    fun participantJoined(callId: String, userId: Long) {
        _activeCall.update { current ->
            if (current?.callId == callId) {
                current.copy(participants = current.participants + userId)  // новый Set!
            } else current
        }
        Log.d("CallDebug", "Participant $userId joined ${callId}, total: ${_activeCall.value?.participants?.size}")
    }

    fun participantLeft(callId: String, userId: Long) {
        _activeCall.update { current ->
            if (current?.callId == callId) {
                current.copy(participants = current.participants - userId)
            } else current
        }
    }

    fun endCall(callId: String, reason: String? = null) {
        scope.launch {
            _callEvents.emit(CallEvent.Ended(callId, reason))
            _activeCall.value = null
        }
    }

    fun showIncomingCallDialog(incomingCall: IncomingCall) {
        _incomingCallDialog.value = incomingCall
        scope.launch {
            _callEvents.emit(CallEvent.IncomingInvite(incomingCall.callId, incomingCall.fromUserId))
        }
    }

    fun dismissIncomingCall() {
        _incomingCallDialog.value = null
    }

    fun clear() {
        scope.cancel()
    }
}
