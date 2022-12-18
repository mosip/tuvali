package com.ble.central.state.message

import android.bluetooth.BluetoothDevice

class ConnectDeviceMessage(var device: BluetoothDevice) : IMessage(CentralStates.CONNECT_DEVICE)
