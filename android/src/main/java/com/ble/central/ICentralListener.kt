package com.ble.central

import android.bluetooth.BluetoothDevice
import java.util.*

interface ICentralListener {
  fun onScanStartedFailed(errorCode: Int)
  fun onDeviceFound(device: BluetoothDevice)
  fun onDeviceConnected(device: BluetoothDevice)
  fun onDeviceDisconnected()
  fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, err: Int)
  fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID)
  fun onServicesDiscovered()
  fun onServicesDiscoveryFailed(errorCode: Int)
  fun onRequestMTUSuccess(mtu: Int)
  fun onRequestMTUFailure(errorCode: Int)
}
