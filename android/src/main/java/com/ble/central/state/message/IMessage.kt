package com.ble.central.state.message

abstract class IMessage(val commandType: CentralStates) {
  enum class CentralStates {
    SCAN_START,
    SCAN_START_SUCCESS,
    SCAN_START_FAILURE,
  }
}
