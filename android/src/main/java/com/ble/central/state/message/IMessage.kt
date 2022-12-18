package com.ble.central.state.message

abstract class IMessage(val commandType: CentralStates) {
  enum class CentralStates {
    SCAN_START,
    SCAN_START_FAILURE,

    DEVICE_FOUND,
    CONNECT_DEVICE,
    DEVICE_CONNECTED,
    DEVICE_DISCONNECTED,

    WRITE,
    WRITE_SUCCESS,
    WRITE_FAILED,
  }
}
