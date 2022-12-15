package com.ble.central.statemessage

abstract class ICentralMessage(val commandType: CentralStates) {
  enum class CentralStates {
    SCAN_START,
    SCAN_START_SUCCESS,
    SCAN_START_FAILURE,
  }
}
