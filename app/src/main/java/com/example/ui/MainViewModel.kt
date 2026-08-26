package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.*
import com.example.domain.transport.ConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemoteSettings(
    val mouseSensitivity: Float = 1.2f,
    val scrollSensitivity: Float = 1.0f,
    val invertScroll: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val tapToClick: Boolean = true
)

class MainViewModel(
    private val connectionManager: ConnectionManager
) : ViewModel() {

    val connectionState = connectionManager.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ConnectionState.DISCONNECTED)

    val connectedDevice = connectionManager.connectedDevice
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableDevices = connectionManager.availableDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _settings = MutableStateFlow(RemoteSettings())
    val settings: StateFlow<RemoteSettings> = _settings.asStateFlow()

    private val _recentIps = MutableStateFlow(listOf("192.168.1.100", "192.168.0.15", "10.0.0.42"))
    val recentIps: StateFlow<List<String>> = _recentIps.asStateFlow()

    init {
        connectionManager.startDiscovery()
    }

    fun updateSettings(newSettings: RemoteSettings) {
        _settings.value = newSettings
    }

    fun updateMouseSensitivity(value: Float) {
        _settings.value = _settings.value.copy(mouseSensitivity = value)
    }

    fun updateScrollSensitivity(value: Float) {
        _settings.value = _settings.value.copy(scrollSensitivity = value)
    }

    fun updateInvertScroll(value: Boolean) {
        _settings.value = _settings.value.copy(invertScroll = value)
    }

    fun updateHaptics(value: Boolean) {
        _settings.value = _settings.value.copy(hapticsEnabled = value)
    }

    fun updateTapToClick(value: Boolean) {
        _settings.value = _settings.value.copy(tapToClick = value)
    }

    fun connectTo(device: Device) {
        viewModelScope.launch {
            connectionManager.connectTo(device)
            addRecentIp(device.ipAddress)
        }
    }

    fun connectToIp(ip: String) {
        viewModelScope.launch {
            connectionManager.connectToIp(ip)
            addRecentIp(ip)
        }
    }

    private fun addRecentIp(ip: String) {
        val current = _recentIps.value.toMutableList()
        current.remove(ip)
        current.add(0, ip)
        _recentIps.value = current.take(5)
    }

    fun disconnect() {
        viewModelScope.launch {
            connectionManager.disconnect()
        }
    }

    fun sendAction(action: ControlAction) {
        viewModelScope.launch {
            connectionManager.sendAction(action)
        }
    }

    fun sendMouseMove(dx: Float, dy: Float) {
        val sens = _settings.value.mouseSensitivity
        sendAction(ControlAction.MouseMove(dx * sens, dy * sens))
    }

    fun sendScroll(dy: Float, dx: Float = 0f) {
        val sens = _settings.value.scrollSensitivity
        val direction = if (_settings.value.invertScroll) -1f else 1f
        sendAction(ControlAction.Scroll(sy = dy * sens * direction, sx = dx * sens))
    }

    fun sendLeftClick() {
        sendAction(ControlAction.MouseClick(MouseButton.LEFT, ClickAction.CLICK))
    }

    fun sendRightClick() {
        sendAction(ControlAction.MouseClick(MouseButton.RIGHT, ClickAction.CLICK))
    }

    fun sendMiddleClick() {
        sendAction(ControlAction.MouseClick(MouseButton.MIDDLE, ClickAction.CLICK))
    }

    fun sendKey(keyCode: String) {
        sendAction(ControlAction.KeyEvent(keyCode, KeyAction.PRESS))
    }

    fun sendShortcut(modifiers: List<String>, keyCode: String) {
        sendAction(ControlAction.KeyEvent(keyCode, KeyAction.PRESS, modifiers))
    }

    fun sendText(text: String) {
        if (text.isNotEmpty()) {
            sendAction(ControlAction.TextInput(text))
        }
    }

    fun sendSystemCommand(command: String) {
        sendAction(ControlAction.SystemCommand(command))
    }
}
