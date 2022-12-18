package com.ble.central.state.message

import android.bluetooth.BluetoothDevice

class DeviceFoundMessage(var device: BluetoothDevice) : IMessage(CentralStates.DEVICE_FOUND)
