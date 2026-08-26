package com.example.domain.capability

import com.example.domain.model.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CapabilityManager {
    private val _currentDeviceCapabilities = MutableStateFlow<Set<String>>(emptySet())
    val currentDeviceCapabilities: StateFlow<Set<String>> = _currentDeviceCapabilities.asStateFlow()

    fun updateCapabilities(device: Device?) {
        if (device == null) {
            _currentDeviceCapabilities.value = emptySet()
        } else {
            _currentDeviceCapabilities.value = device.capabilities.toSet()
        }
    }

    fun hasCapability(capability: String): Boolean {
        return _currentDeviceCapabilities.value.contains(capability)
    }
}
