package com.ble.central.state.message

import android.bluetooth.BluetoothDevice

class DeviceDisconnectedMessage(device: BluetoothDevice) : IMessage(CentralStates.DEVICE_DISCONNECTED)
