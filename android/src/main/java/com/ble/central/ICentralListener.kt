package com.ble.central

import android.bluetooth.BluetoothDevice

interface ICentralListener {
  fun onScanStartedFailed(errorCode: Int)
  fun onDeviceFound(device: BluetoothDevice)
  fun onDeviceConnected()
  fun onDeviceDisconnected()
  fun onKeyExchanged()
}
