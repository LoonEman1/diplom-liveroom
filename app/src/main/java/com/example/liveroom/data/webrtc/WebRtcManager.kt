package com.example.liveroom.data.webrtc

import android.content.Context
import android.util.Log
import com.example.liveroom.data.model.CallKind
import com.example.liveroom.data.model.IceCandidateDto
import dagger.hilt.android.qualifiers.ApplicationContext
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRtcManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val eglBase: EglBase = EglBase.create()
    private val peerConnectionFactory: PeerConnectionFactory

    // Делегат для отправки сигналов через ServerViewModel
    var signalingDelegate: SignalingDelegate? = null

    private val peerConnections = mutableMapOf<String, PeerConnection>()
    private val remoteUserIds = mutableMapOf<String, Long>()

    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null

    private val pendingIceCandidates = mutableMapOf<String, MutableList<IceCandidate>>()

    init {
        // 1. Инициализация WebRTC
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        // 2. Фабрика кодеков
        val encoderFactory = DefaultVideoEncoderFactory(
            eglBase.eglBaseContext,
            true,
            true
        )
        val decoderFactory = DefaultVideoDecoderFactory(eglBase.eglBaseContext)

        // 3. Создание PeerConnectionFactory
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        // 4. Подготовка микрофона
        createLocalAudioTrack()
    }

    interface SignalingDelegate {
        fun sendOffer(callId: String, toUserId: Long, sdp: String)
        fun sendAnswer(callId: String, toUserId: Long, sdp: String)
        fun sendIceCandidate(callId: String, toUserId: Long, ice: IceCandidateDto)
    }

    private fun createLocalAudioTrack() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
        }

        localAudioSource = peerConnectionFactory.createAudioSource(constraints)
        localAudioTrack = peerConnectionFactory.createAudioTrack("local_audio_track", localAudioSource)
        localAudioTrack?.setEnabled(true)
    }

    private fun getOrCreatePeerConnection(callId: String, remoteUserId: Long): PeerConnection {
        remoteUserIds[callId] = remoteUserId
        peerConnections[callId]?.let { return it }

        val iceServers = listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
                .createIceServer(),

            PeerConnection.IceServer.builder("turn:numb.viagenie.ca")
                .setUsername("liveroomtes321421t@gmail.com")
                .setPassword("muaz345@webrtc")
                .createIceServer(),

            PeerConnection.IceServer.builder("turn:numb.viagenie.ca:443?transport=tcp")
                .setUsername("liveroomtest4214@gmail.com")
                .setPassword("muaz512521@webrtc")
                .createIceServer()
        )



        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            iceTransportsType = PeerConnection.IceTransportsType.ALL
        }

        val observer = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                Log.d("WebRTC", "❄️ Local ICE candidate generated")

                Log.d("WebRTC", "❄️ ICE: ${candidate.sdp}")
                signalingDelegate?.sendIceCandidate(
                    callId = callId,
                    toUserId = remoteUserId,
                    ice = IceCandidateDto(
                        candidate = candidate.sdp,
                        sdpMid = candidate.sdpMid,
                        sdpMLineIndex = candidate.sdpMLineIndex
                    )
                )
            }

            override fun onTrack(transceiver: org.webrtc.RtpTransceiver?) {
                Log.d("WebRTC", "🎧 Remote track received")
            }

            override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                Log.d("WebRTC", "🔄 PC State: $newState")
            }

            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {
                Log.d("WebRTC", "🧊 ICE State: $newState")
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onAddStream(p0: MediaStream?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {}
        }

        val pc = peerConnectionFactory.createPeerConnection(rtcConfig, observer)
            ?: error("Failed to create PeerConnection")

        // ✅ Unified Plan → addTrack instead of addStream
        localAudioTrack?.let { track ->
            pc.addTrack(track, listOf("local_stream_$callId"))
        }

        peerConnections[callId] = pc
        return pc
    }

    fun createOffer(callId: String, remoteUserId: Long, kind: CallKind = CallKind.AUDIO) {
        val pc = getOrCreatePeerConnection(callId, remoteUserId)

        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                pc.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d("WebRTC", "📤 Sending OFFER")
                        signalingDelegate?.sendOffer(callId, remoteUserId, desc.description)
                    }
                    override fun onSetFailure(p0: String?) {}
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, desc)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    fun onRemoteOffer(callId: String, fromUserId: Long, sdp: String, kind: CallKind = CallKind.AUDIO) {
        val pc = getOrCreatePeerConnection(callId, fromUserId)
        val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, sdp)

        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d("WebRTC", "📥 Remote OFFER set")
                // ✅ ВАЖНО: Сначала проливаем ICE, потом создаем ответ
                drainIceCandidates(callId, pc)
                createAnswer(callId, fromUserId, kind)
            }
            override fun onSetFailure(error: String?) {
                Log.e("WebRTC", "❌ setRemoteDescription FAILED: $error")  // ← ДОБАВЬ!
            }
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, remoteDesc)
    }

    private fun createAnswer(callId: String, remoteUserId: Long, kind: CallKind) {
        val pc = peerConnections[callId] ?: return
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }

        pc.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                pc.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        Log.d("WebRTC", "📤 Sending ANSWER")
                        signalingDelegate?.sendAnswer(callId, remoteUserId, desc.description)
                    }
                    override fun onSetFailure(p0: String?) {}
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, desc)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    fun onRemoteAnswer(callId: String, sdp: String) {
        val pc = peerConnections[callId] ?: return
        val remoteDesc = SessionDescription(SessionDescription.Type.ANSWER, sdp)

        pc.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                Log.d("WebRTC", "✅ Remote ANSWER applied")
                // ✅ ВАЖНО: Проливаем накопленные кандидаты
                drainIceCandidates(callId, pc)
            }
            override fun onSetFailure(p0: String?) {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, remoteDesc)
    }

    fun onRemoteIce(callId: String, ice: IceCandidateDto) {
        val pc = peerConnections[callId]
        val candidate = IceCandidate(ice.sdpMid, ice.sdpMLineIndex, ice.candidate)

        // Если PC еще не создан или мы еще не установили RemoteDescription,
        // складываем кандидатов в "копилку"
        if (pc == null || pc.remoteDescription == null) {
            Log.d("WebRTC", "❄️ ICE queued: PC or RemoteDesc not ready for $callId")
            pendingIceCandidates.getOrPut(callId) { mutableListOf() }.add(candidate)
        } else {
            pc.addIceCandidate(candidate)
            Log.d("WebRTC", "❄️ ICE added directly")
        }
    }

    private fun drainIceCandidates(callId: String, pc: PeerConnection) {
        val candidates = pendingIceCandidates.remove(callId)
        candidates?.forEach {
            pc.addIceCandidate(it)
            Log.d("WebRTC", "❄️ Drained ICE candidate for $callId")
        }
    }

    fun closeCall(callId: String) {
        peerConnections.remove(callId)?.close()
        remoteUserIds.remove(callId)
    }
}
