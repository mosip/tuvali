package com.ble.peripheral.statemessage

abstract class IPeripheralMessage(val commandType: PeripheralStates) {
  enum class PeripheralStates {
    ADV_START,
    ADV_START_SUCCESS,
    ADV_START_FAILURE,
  }
}
