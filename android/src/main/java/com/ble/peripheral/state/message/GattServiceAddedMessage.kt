package com.ble.peripheral.state.message

class GattServiceAddedMessage(val status: Int): IMessage(PeripheralMessageTypes.SERVICE_ADD_STATUS){}
