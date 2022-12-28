package io.mosip.ble.central.state.message

import android.bluetooth.BluetoothDevice

class DeviceConnectedMessage(val device: BluetoothDevice) : IMessage(CentralStates.DEVICE_CONNECTED)
