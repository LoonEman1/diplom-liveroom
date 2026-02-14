package com.example.liveroom.data.local

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


    private var isConnected = false

    fun connect() {
        val token = tokenManager.getAccessToken() ?: run {
            Log.e("WS", "❌ Нет токена!")
            return
        }

        Log.d("WS", "🔌 Токен: ${token.take(20)}...")

        val request = Request.Builder()
            .url("wss://nighthunting23.ru/ws")
            .addHeader("Authorization", "Bearer $token")
            .build()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WS", "✅ OPEN: ${response.code}")
                isConnected = true

                val connectFrame = buildString {
                    append("CONNECT\n")
                    append("accept-version:1.2\n")
                    append("heart-beat:10000,10000\n")
                    append("\n\u0000")
                }
                webSocket.send(connectFrame)
                _logs.tryEmit(">>> CONNECT отправлен")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WS", "📨 $text")
                _logs.tryEmit(text)
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WS", "📦 ${bytes.hex()}")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WS", "❌ ${t.message}")
                Log.e("WS", "CODE: ${response?.code}")
                response?.body?.string()?.let { body ->
                    Log.e("WS", "BODY: $body")
                }
                isConnected = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WS", "🔌 CLOSED $code")
            }
        })
    }

    fun subscribe(topic: String) {
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
        val frame = buildString {
            append("SEND\n")
            append("destination:$appPath\n")
            append("content-type:application/json\n")
            append("\n$jsonBody\u0000")
        }
        ws?.send(frame)
        Log.d("WS", "📤 SEND $appPath")
    }

    fun disconnect() {
        ws?.send("DISCONNECT\n\n\u0000")
        ws?.close(1000, "bye")
    }
}

