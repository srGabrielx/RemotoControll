package com.example.domain.transport

import com.example.domain.model.ConnectionState
import com.example.domain.model.ControlAction
import com.example.domain.model.Device
import kotlinx.coroutines.flow.StateFlow

interface Transport {
    val state: StateFlow<ConnectionState>
    
    suspend fun connect(device: Device)
    suspend fun disconnect()
    suspend fun sendAction(action: ControlAction)
}

interface ConnectionManager {
    val connectionState: StateFlow<ConnectionState>
    val connectedDevice: StateFlow<Device?>
    val availableDevices: StateFlow<List<Device>>

    fun startDiscovery()
    fun stopDiscovery()
    suspend fun connectTo(device: Device)
    suspend fun connectToIp(ip: String)
    suspend fun disconnect()
    suspend fun sendAction(action: ControlAction)
}
