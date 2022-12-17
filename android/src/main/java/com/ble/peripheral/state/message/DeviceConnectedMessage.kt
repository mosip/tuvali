package com.ble.peripheral.state.message

import android.bluetooth.BluetoothGattService

class DeviceConnectedMessage(val status: Int, val newState: Int): IMessage(PeripheralMessageTypes.DEVICE_CONNECTED){}
