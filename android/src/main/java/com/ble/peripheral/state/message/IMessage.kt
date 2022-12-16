package com.ble.peripheral.state.message

abstract class IMessage(val commandType: PeripheralStates) {
  enum class PeripheralStates {
    ADV_START,
    ADV_START_SUCCESS,
    ADV_START_FAILURE,
  }
}
