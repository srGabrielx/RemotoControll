package com.example.domain.adapter

import com.example.domain.model.ControlAction
import com.example.domain.transport.Transport

abstract class DeviceAdapter(protected val transport: Transport) {
    abstract val supportedCapabilities: List<String>
    abstract suspend fun translateAndSend(action: ControlAction)
}
