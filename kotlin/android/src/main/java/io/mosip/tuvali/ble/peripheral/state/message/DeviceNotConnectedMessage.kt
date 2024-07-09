package io.mosip.tuvali.ble.peripheral.state.message

class DeviceNotConnectedMessage(val status: Int, val newState: Int): IMessage(PeripheralMessageTypes.DEVICE_NOT_CONNECTED){}
