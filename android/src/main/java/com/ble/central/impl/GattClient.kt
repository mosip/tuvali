package com.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log

class GattClient(var context: Context) {
  private lateinit var onDeviceDisconnected: (BluetoothDevice) -> Unit
  private lateinit var onDeviceConnected: (BluetoothDevice) -> Unit;
  private var peripheral: BluetoothDevice? = null;

  private val bluetoothGattCallback = object : BluetoothGattCallback() {
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
      super.onMtuChanged(gatt, mtu, status)
      peripheral?.let { onDeviceConnected(it) }

      Log.i("BLE Central", "BLE: Successfully changed mtu size: $mtu")
    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
      super.onServicesDiscovered(gatt, status)
      if (status != BluetoothGatt.GATT_SUCCESS) {
        Log.e("BLE Central", "BLE: Failed to discover services")
        return
      }

      val success = gatt?.requestMtu(517)

      if (success == false) {
        Log.i("BLE Central", "BLE: Failed to request MTU change")
      }

      Log.i("BLE Central", "BLE: discovered services: ${gatt?.services?.map { it.uuid }}")
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
      if (newState == BluetoothProfile.STATE_CONNECTED) {
        Log.i("BLE Central", "BLE: Connected to the peripheral")
        gatt?.discoverServices()
      } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        Log.i("BLE Central", "BLE: Disconnected from the peripheral")

        gatt?.disconnect()
        gatt?.close()

        peripheral?.let { onDeviceDisconnected(it) };
        peripheral = null;
      }
    }
  }

  @SuppressLint("MissingPermission", "NewApi")
  fun connect(
    device: BluetoothDevice,
    onDeviceConnected: (BluetoothDevice) -> Unit,
    onDeviceDisconnected: (BluetoothDevice) -> Unit
  ) {
    Log.i("BLE Central", "calling connect to ble peripheral")

    this.onDeviceConnected = onDeviceConnected;
    this.onDeviceDisconnected = onDeviceDisconnected;
    peripheral = device;

    val gatt = device.connectGatt(
      context,
      false,
      bluetoothGattCallback,
      BluetoothDevice.TRANSPORT_LE
    )

    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
  }
}
