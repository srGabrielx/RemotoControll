package com.example.data.transport

import android.util.Log
import com.example.domain.model.*
import com.example.domain.transport.Transport
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import java.util.UUID

class WebSocketTransport(private val client: OkHttpClient) : Transport {
    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val state: StateFlow<ConnectionState> = _state

    private var webSocket: WebSocket? = null
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var sequence = 0L
    private val sessionId = UUID.randomUUID().toString()

    override suspend fun connect(device: Device) {
        _state.value = ConnectionState.CONNECTING
        val request = Request.Builder()
            .url("ws://${device.ipAddress}:8080/remote") // Default port for now
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _state.value = ConnectionState.CONNECTED
                // Send HELLO
                val hello = mapOf(
                    "protocolVersion" to "1.0",
                    "sessionId" to sessionId,
                    "type" to "HELLO",
                    "payload" to mapOf(
                        "deviceName" to "Android Client",
                        "capabilities" to listOf("mouse", "keyboard", "screen")
                    )
                )
                webSocket.send(moshi.adapter(Map::class.java).toJson(hello))
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Received: $text")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _state.value = ConnectionState.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _state.value = ConnectionState.ERROR
            }
        })
    }

    override suspend fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _state.value = ConnectionState.DISCONNECTED
    }

    override suspend fun sendAction(action: ControlAction) {
        if (_state.value != ConnectionState.CONNECTED) return

        val type: String
        val payload: Map<String, Any>

        when (action) {
            is ControlAction.MouseMove -> {
                type = "MOUSE_MOVE"
                payload = mapOf("dx" to action.dx, "dy" to action.dy)
            }
            is ControlAction.MouseClick -> {
                type = "MOUSE_CLICK"
                payload = mapOf("button" to action.button.name, "action" to action.action.name)
            }
            is ControlAction.Scroll -> {
                type = "MOUSE_SCROLL"
                payload = mapOf("sx" to action.sx, "sy" to action.sy)
            }
            is ControlAction.KeyEvent -> {
                type = "KEY_EVENT"
                payload = mapOf("keyCode" to action.keyCode, "action" to action.action.name, "modifiers" to action.modifiers)
            }
            is ControlAction.TextInput -> {
                type = "TEXT_INPUT"
                payload = mapOf("text" to action.text)
            }
            is ControlAction.SystemCommand -> {
                type = "SYSTEM_COMMAND"
                payload = mapOf("command" to action.command)
            }
        }

        val message = mapOf(
            "protocolVersion" to "1.0",
            "sessionId" to sessionId,
            "sequence" to ++sequence,
            "timestamp" to System.currentTimeMillis(),
            "type" to type,
            "payload" to payload
        )

        val json = moshi.adapter(Map::class.java).toJson(message)
        scope.launch {
            webSocket?.send(json)
        }
    }
}
