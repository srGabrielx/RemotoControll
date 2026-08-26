package com.example.data.manager

import com.example.domain.model.ConnectionState
import com.example.domain.model.ControlAction
import com.example.domain.model.Device
import com.example.domain.transport.ConnectionManager
import com.example.domain.transport.Transport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DefaultConnectionManager(
    private val transport: Transport
) : ConnectionManager {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val connectionState: StateFlow<ConnectionState> = transport.state

    private val _connectedDevice = MutableStateFlow<Device?>(null)
    override val connectedDevice: StateFlow<Device?> = _connectedDevice.asStateFlow()

    private val _availableDevices = MutableStateFlow<List<Device>>(emptyList())
    override val availableDevices: StateFlow<List<Device>> = _availableDevices.asStateFlow()

    init {
        // In a real implementation, we would hook up NsdManager here
        // to update _availableDevices
    }

    override fun startDiscovery() {
        // Placeholder for NsdManager discovery
        _availableDevices.value = listOf(
            Device("dev1", "Desktop PC", "192.168.1.100", listOf("mouse", "keyboard", "screen"))
        )
    }

    override fun stopDiscovery() {
        // Stop discovery
    }

    override suspend fun connectTo(device: Device) {
        _connectedDevice.value = device
        transport.connect(device)
    }

    override suspend fun connectToIp(ip: String) {
        val device = Device("manual", "Manual IP", ip, emptyList())
        connectTo(device)
    }

    override suspend fun disconnect() {
        transport.disconnect()
        _connectedDevice.value = null
    }

    override suspend fun sendAction(action: ControlAction) {
        transport.sendAction(action)
    }
}
