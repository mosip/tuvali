package com.ble.peripheral.state.message

import android.bluetooth.BluetoothGattService

class DeviceNotConnectedMessage(val status: Int, val newState: Int): IMessage(PeripheralMessageTypes.DEVICE_NOT_CONNECTED){}
