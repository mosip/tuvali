package com.ble.central.state.message

import android.bluetooth.BluetoothDevice

class DeviceConnectedMessage(device: BluetoothDevice) : IMessage(CentralStates.DEVICE_CONNECTED)
