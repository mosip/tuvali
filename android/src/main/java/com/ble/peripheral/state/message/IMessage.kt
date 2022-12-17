package com.ble.peripheral.state.message

abstract class IMessage(val commandType: PeripheralMessageTypes) {
  enum class PeripheralMessageTypes {
    SETUP_SERVICE,
    SERVICE_ADD_STATUS,
    ADV_START,
    ADV_START_SUCCESS,
    ADV_START_FAILURE,
    DEVICE_CONNECTED,
    DEVICE_NOT_CONNECTED,
    RECEIVED_WRITE,
    ON_READ,
    ENABLE_COMMUNICATION
  }
}
