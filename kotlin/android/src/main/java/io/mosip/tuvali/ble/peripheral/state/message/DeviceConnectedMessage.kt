package io.mosip.tuvali.ble.peripheral.state.message

class DeviceConnectedMessage(val status: Int, val newState: Int): IMessage(PeripheralMessageTypes.DEVICE_CONNECTED){}
