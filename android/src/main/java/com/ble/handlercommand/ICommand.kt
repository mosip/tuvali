package com.ble.handlercommand

abstract class ICommand(val commandType: PeripheralStates) {
  enum class PeripheralStates {
    ADV_START,
    ADV_START_SUCCESS,
    ADV_START_FAILURE,
  }
}
