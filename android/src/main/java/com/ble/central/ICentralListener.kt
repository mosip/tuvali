package com.ble.central

import android.bluetooth.BluetoothDevice
import java.util.*

interface ICentralListener {
  fun onScanStartedFailed(errorCode: Int)
  fun onDeviceFound(device: BluetoothDevice)
  fun onDeviceConnected(device: BluetoothDevice)
  fun onDeviceDisconnected()
  fun onKeyExchanged()
  fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, err: Int)
  fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID)
}
