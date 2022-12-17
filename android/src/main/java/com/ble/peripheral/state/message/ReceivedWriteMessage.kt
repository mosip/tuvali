package com.ble.peripheral.state.message

import android.bluetooth.BluetoothGattCharacteristic

class ReceivedWriteMessage(val characteristic: BluetoothGattCharacteristic?, val data: ByteArray?): IMessage(PeripheralMessageTypes.RECEIVED_WRITE) {}
