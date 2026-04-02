package com.example.liveroom.data.local

import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val tokenManager: TokenManager
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var ws: WebSocket? = null

    private val _logs = MutableSharedFlow<String>()
    val logs = _logs.asSharedFlow()

    var onMessageReceived: ((String) -> Unit)? = null

    private var isConnected = false

    // 🔄 Реконнект
    private var reconnectAttempts = 0
    private val maxReconnects = 10
    private val reconnectHandler = Handler(Looper.getMainLooper())
    private var reconnectRunnable: Runnable? = null
    private var heartbeatRunnable: Runnable? = null

    fun connect() {
        val token = tokenManager.getAccessToken() ?: run {
            Log.e("WS", "❌ Нет токена!")
            return
        }

        Log.d("WS", "🔌 Connect (attempt ${reconnectAttempts + 1})")

        val request = Request.Builder()
            .url("wss://nighthunting23.ru/ws")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WS", "✅ OPEN: ${response.code}")
                isConnected = true
                reconnectAttempts = 0 // ✅ Сброс счётчика

                val connectFrame = buildString {
                    append("CONNECT\n")
                    append("accept-version:1.2\n")
                    append("heart-beat:10000,10000\n")
                    append("\n\u0000")
                }
                webSocket.send(connectFrame)
                _logs.tryEmit(">>> CONNECT отправлен")

                // ✅ Запуск heartbeat
                startHeartbeat()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WS", "📨 $text")
                _logs.tryEmit(text)
                onMessageReceived?.invoke(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WS", "📦 ${bytes.hex()}")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WS", "❌ Failure: ${t.message}")
                Log.e("WS", "CODE: ${response?.code}")
                isConnected = false
                scheduleReconnect()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WS", "🔌 CLOSED $code: $reason")
                isConnected = false
                stopHeartbeat()
                scheduleReconnect()
            }
        })
    }

    // 🔄 Автореконнект
    private fun scheduleReconnect() {
        if (reconnectAttempts >= maxReconnects) {
            Log.e("WS", "❌ Max reconnects ($maxReconnects) reached")
            return
        }

        reconnectAttempts++
        val delayMs = (1000L * reconnectAttempts).coerceAtMost(30000L) // 1s, 2s... max 30s

        Log.d("WS", "🔄 Reconnect #$reconnectAttempts in ${delayMs/1000}s...")

        reconnectRunnable?.let { reconnectHandler.removeCallbacks(it) }
        reconnectRunnable = Runnable {
            Log.d("WS", "🔄 Reconnecting...")
            connect()
        }
        reconnectHandler.postDelayed(reconnectRunnable!!, delayMs)
    }

    private fun startHeartbeat() {
        heartbeatRunnable = object : Runnable {
            override fun run() {
                if (isConnected) {
                    ws?.send("SEND\nheart-beat:true\n\n${'\u0000'}")
                    Log.v("WS", "💓 Heartbeat sent")
                }
                reconnectHandler.postDelayed(this, 25000) // 25 сек
            }
        }
        reconnectHandler.postDelayed(heartbeatRunnable!!, 25000)
    }

    private fun stopHeartbeat() {
        heartbeatRunnable?.let {
            reconnectHandler.removeCallbacks(it)
            heartbeatRunnable = null
        }
    }

    fun requestActiveCalls(serverId: Int, conversationId: Int) {
        val topic = "/topic/servers/$serverId/conversations/$conversationId/calls/active"
        subscribe(topic)
        Log.d("WS", "🔍 Подписка на активные звонки")
    }

    fun subscribe(topic: String) {
        if (!isConnected) {
            Log.w("WS", "⚠️ Not connected, skipping SUB $topic")
            return
        }

        val frame = buildString {
            append("SUBSCRIBE\n")
            append("id:${System.currentTimeMillis()}\n")
            append("destination:$topic\n")
            append("\n\u0000")
        }
        ws?.send(frame)
        Log.d("WS", "📡 SUB $topic")
    }

    fun send(appPath: String, jsonBody: String) {
        if (!isConnected) return

        val frame = StringBuilder().apply {
            append("SEND\n")
            append("destination:$appPath\n")
            append("content-type:application/json\n")
            append("content-length:${jsonBody.toByteArray().size}\n")
            append("\n")
            append(jsonBody)
            append("\u0000")
        }.toString()

        ws?.send(frame)
        Log.d("WS", "📤 SEND to $appPath")
    }

    fun disconnect() {
        Log.d("WS", "🔌 Manual disconnect")
        reconnectHandler.removeCallbacksAndMessages(null)
        stopHeartbeat()
        reconnectAttempts = 0
        ws?.send("DISCONNECT\n\n\u0000")
        ws?.close(1000, "Manual disconnect")
        ws = null
    }

    fun manualReconnect() {
        Log.d("WS", "🔄 Manual reconnect")
        disconnect()
        connect()
    }

    fun isConnected() = isConnected
}
