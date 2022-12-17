package com.ble.peripheral.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log

@SuppressLint("MissingPermission")
class GattServer(private val context: Context) : BluetoothGattServerCallback() {
  private val logTag = "GattServer"
  private lateinit var gattServer: BluetoothGattServer
  private lateinit var bluetoothDevice: BluetoothDevice

  private lateinit var onServiceAddedCallback: (Int) -> Unit
  private lateinit var onDeviceConnectedCallback: (Int, Int) -> Unit
  private lateinit var onDeviceNotConnectedCallback: (Int, Int) -> Unit
  private lateinit var onReceivedWriteCallback: (BluetoothGattCharacteristic?, ByteArray?) -> Unit


  fun start(onDeviceConnected: (Int, Int) -> Unit, onDeviceNotConnected: (Int, Int) -> Unit, onReceivedWrite: (BluetoothGattCharacteristic?, ByteArray?) -> Unit) {
    onDeviceConnectedCallback = onDeviceConnected
    onDeviceNotConnectedCallback = onDeviceNotConnected
    onReceivedWriteCallback = onReceivedWrite
    val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    gattServer = bluetoothManager.openGattServer(context, this@GattServer)
  }

  fun stop() {
    // TODO: Handle explicit connection close
    gattServer.close()
  }

  fun addService(service: BluetoothGattService, onServiceAdded: (Int) -> Unit) {
    onServiceAddedCallback = onServiceAdded
    gattServer.addService(service)
  }

  override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
    onServiceAddedCallback(status)
  }

  override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
    Log.d(logTag, "onConnectionStateChange: status: $status, newState: $newState")
    if(newState == BluetoothProfile.STATE_CONNECTED){
      bluetoothDevice.let { device }
    } else {
      bluetoothDevice.let { null }
    }
  }

  override fun onCharacteristicWriteRequest(
    device: BluetoothDevice?,
    requestId: Int,
    characteristic: BluetoothGattCharacteristic?,
    preparedWrite: Boolean,
    responseNeeded: Boolean,
    offset: Int,
    value: ByteArray?
  ) {
    Log.d(logTag, "onCharacteristicWriteRequest: requestId: ${requestId}, preparedWrite: ${preparedWrite}, responseNeeded: ${responseNeeded}, offset: ${offset}, dataSize: ${value?.size}")
    onReceivedWriteCallback(characteristic, value)
    if (responseNeeded) {
      gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
    }
  }
}
