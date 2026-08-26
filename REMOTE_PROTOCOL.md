# Universal Remote Protocol (V1)

This document describes the application-layer protocol used between the Android Universal Remote client and the Desktop/Host Receiver (Windows/Linux/macOS).

## Architecture

The protocol is designed to be transport-agnostic. The payload structures remain the same whether the data is transmitted via:
- Local Area Network (WebSocket / TCP)
- WebRTC Data Channels
- Bluetooth RFCOMM
- Bluetooth Low Energy (GATT)

## Message Format

All messages are JSON objects containing the following base structure:

```json
{
  "protocolVersion": "1.0",
  "sessionId": "uuid-string",
  "deviceId": "android-device-id",
  "sequence": 12345,
  "timestamp": 1714567890123,
  "type": "ACTION_TYPE",
  "payload": { ... }
}
```

### 1. Connection & Capabilities

**Client Hello (Sent upon connection)**
```json
{
  "type": "HELLO",
  "payload": {
    "deviceName": "My Phone",
    "capabilities": ["mouse", "keyboard", "screen", "media"]
  }
}
```

**Host Acknowledge (Host -> Client)**
```json
{
  "type": "HELLO_ACK",
  "payload": {
    "hostName": "Desktop-PC",
    "capabilities": ["mouse", "keyboard", "screen"],
    "requiresAuth": false
  }
}
```

### 2. Mouse / Touchpad Input

For low-latency continuous movement, implementations may choose to batch or pack these into binary structures over UDP/WebRTC, but for JSON/WebSocket, it looks like this:

**Move Pointer (Relative delta)**
```json
{
  "type": "MOUSE_MOVE",
  "payload": {
    "dx": 12.5,
    "dy": -5.0
  }
}
```

**Mouse Click / Touch**
```json
{
  "type": "MOUSE_CLICK",
  "payload": {
    "button": "LEFT", // LEFT, RIGHT, MIDDLE
    "action": "DOWN"  // DOWN, UP, CLICK, DOUBLE_CLICK
  }
}
```

**Scroll**
```json
{
  "type": "MOUSE_SCROLL",
  "payload": {
    "sx": 0.0,
    "sy": -1.0
  }
}
```

### 3. Keyboard Input

**Key Event**
```json
{
  "type": "KEY_EVENT",
  "payload": {
    "keyCode": "ENTER", // Standardized key names
    "action": "DOWN",   // DOWN, UP, PRESS
    "modifiers": ["SHIFT", "CTRL"]
  }
}
```

**Text Input (Bulk typing)**
```json
{
  "type": "TEXT_INPUT",
  "payload": {
    "text": "Hello world"
  }
}
```

### 4. Screen Mode (WebRTC Signaling Placeholder)

```json
{
  "type": "WEBRTC_OFFER",
  "payload": {
    "sdp": "..."
  }
}
```

## Security

- Devices should implement an initial pairing process using a 6-digit PIN or QR code.
- After pairing, the host issues a long-lived `deviceId` and token.
- Future connections must provide the token in the `HELLO` message payload.
