package com.example.domain.model

sealed class ControlAction {
    // Mouse
    data class MouseMove(val dx: Float, val dy: Float) : ControlAction()
    data class MouseClick(val button: MouseButton, val action: ClickAction) : ControlAction()
    data class Scroll(val sy: Float, val sx: Float = 0f) : ControlAction()

    // Keyboard
    data class KeyEvent(val keyCode: String, val action: KeyAction, val modifiers: List<String> = emptyList()) : ControlAction()
    data class TextInput(val text: String) : ControlAction()

    // System
    data class SystemCommand(val command: String) : ControlAction()
}

enum class MouseButton { LEFT, RIGHT, MIDDLE }
enum class ClickAction { DOWN, UP, CLICK, DOUBLE_CLICK }
enum class KeyAction { DOWN, UP, PRESS }

data class Device(
    val id: String,
    val name: String,
    val ipAddress: String,
    val capabilities: List<String>
)

enum class ConnectionState {
    DISCONNECTED,
    DISCOVERING,
    PAIRING,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}
