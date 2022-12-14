package com.ble.peripheral

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.content.Context

@SuppressLint("MissingPermission")
class GattServer(private val context: Context) : BluetoothGattServerCallback() {
  private lateinit var gattServer: BluetoothGattServer

  fun start() {
    val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    gattServer = bluetoothManager.openGattServer(context, this@GattServer)
  }

  override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
    super.onConnectionStateChange(device, status, newState)
  }
}
