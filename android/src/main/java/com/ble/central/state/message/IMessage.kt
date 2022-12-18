package com.ble.central.state.message

abstract class IMessage(val commandType: CentralStates) {
  enum class CentralStates {
    SCAN_START,
    DEVICE_FOUND,
    CONNECT_DEVICE,
    DEVICE_CONNECTED,
    DEVICE_DISCONNECTED,
    SCAN_START_FAILURE,
  }
}
