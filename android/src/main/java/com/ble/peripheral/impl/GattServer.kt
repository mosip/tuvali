package com.ble.peripheral.impl

import android.annotation.SuppressLint
import android.bluetooth.*
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
