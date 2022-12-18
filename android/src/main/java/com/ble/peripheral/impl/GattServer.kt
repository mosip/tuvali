package com.ble.peripheral.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.UUID

@OptIn(ExperimentalUnsignedTypes::class)
@SuppressLint("MissingPermission")
class GattServer(private val context: Context) : BluetoothGattServerCallback() {
  private val logTag = "GattServer"
  private lateinit var gattServer: BluetoothGattServer
  private var bluetoothDevice: BluetoothDevice? = null

  private lateinit var onServiceAddedCallback: (Int) -> Unit
  private lateinit var onDeviceConnectedCallback: (Int, Int) -> Unit
  private lateinit var onDeviceNotConnectedCallback: (Int, Int) -> Unit
  private lateinit var onReceivedWriteCallback: (BluetoothGattCharacteristic?, ByteArray?) -> Unit
  private lateinit var onReadCallback: (BluetoothGattCharacteristic?, Boolean) -> Unit


  fun start(
    onDeviceConnected: (Int, Int) -> Unit,
    onDeviceNotConnected: (Int, Int) -> Unit,
    onReceivedWrite: (BluetoothGattCharacteristic?, ByteArray?) -> Unit,
    onRead: (BluetoothGattCharacteristic?, Boolean) -> Unit
  ) {
    onDeviceConnectedCallback = onDeviceConnected
    onDeviceNotConnectedCallback = onDeviceNotConnected
    onReceivedWriteCallback = onReceivedWrite
    onReadCallback = onRead
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

  fun writeToChar(serviceUUID: UUID, charUUID: UUID, data: UByteArray): Boolean {
    val service = gattServer.getService(serviceUUID)
    val characteristic = service.getCharacteristic(charUUID)
    characteristic.value = data.toByteArray()
    return gattServer.notifyCharacteristicChanged(bluetoothDevice, characteristic, false)
  }

  override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
    onServiceAddedCallback(status)
  }

  override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
    Log.d(logTag, "onConnectionStateChange: status: $status, newState: $newState")
    bluetoothDevice = if(newState == BluetoothProfile.STATE_CONNECTED){
      onDeviceConnectedCallback(status, newState)
      device
    } else {
      onDeviceNotConnectedCallback(status, newState)
      null
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

  // TODO: Confirm if this is needed, if peripheral always sends data through notification
  override fun onCharacteristicReadRequest(
    device: BluetoothDevice?,
    requestId: Int,
    offset: Int,
    characteristic: BluetoothGattCharacteristic?
  ) {
    val isSuccessful = gattServer.sendResponse(
      device,
      requestId,
      BluetoothGatt.GATT_SUCCESS,
      offset,
      characteristic?.value
    )
    onReadCallback(characteristic, isSuccessful)
  }

  override fun onDescriptorWriteRequest(
    device: BluetoothDevice?,
    requestId: Int,
    descriptor: BluetoothGattDescriptor?,
    preparedWrite: Boolean,
    responseNeeded: Boolean,
    offset: Int,
    value: ByteArray?
  ) {
    if (responseNeeded) {
      gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
    }
  }
}
