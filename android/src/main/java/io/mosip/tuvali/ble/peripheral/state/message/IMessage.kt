package io.mosip.tuvali.ble.peripheral.state.message

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
    ENABLE_COMMUNICATION,
    SEND_DATA,
    SEND_DATA_NOTIFIED,

    ADV_STOP,
    DISCONNECT,
    CLOSE_SERVER
  }
}
