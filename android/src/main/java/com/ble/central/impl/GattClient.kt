package com.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.*

class GattClient(var context: Context) {
  private lateinit var onWriteFailed: (BluetoothDevice, UUID, Int) -> Unit
  private lateinit var onWriteSuccess: (BluetoothDevice, UUID) -> Unit
  private lateinit var onDeviceDisconnected: (BluetoothDevice) -> Unit
  private lateinit var onDeviceConnected: (BluetoothDevice) -> Unit;
  private var peripheral: BluetoothDevice? = null;
  private var bluetoothGatt: BluetoothGatt? = null;

  private val bluetoothGattCallback = object : BluetoothGattCallback() {
    override fun onCharacteristicWrite(
      gatt: BluetoothGatt?,
      characteristic: BluetoothGattCharacteristic?,
      status: Int
    ) {
      Log.i("BLE Central", "Status of write is $status for ${characteristic?.uuid}")

      if(status != BluetoothGatt.GATT_SUCCESS) {
        Log.i("BLE Central", "\"Failed to send message to peripheral")

        peripheral?.let {
          characteristic?.uuid?.let {
              uuid -> onWriteFailed(it, uuid, status)
          } }
      }

      peripheral?.let {
        characteristic?.let {
            char -> onWriteSuccess(it, char.uuid) }
      }
    }

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
        closeConnection()

        peripheral = null;
      }
    }
  }

  @SuppressLint("MissingPermission")
  private fun closeConnection() {
    bluetoothGatt?.disconnect()
    bluetoothGatt?.close()
    peripheral?.let { onDeviceDisconnected(it) };

    bluetoothGatt = null

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
    this.bluetoothGatt = gatt
  }

  @SuppressLint("MissingPermission")
  fun write(
    device: BluetoothDevice,
    serviceUuid: UUID,
    charUUID: UUID,
    data: String,
    onSuccess: (BluetoothDevice, UUID) -> Unit,
    onFailed: (BluetoothDevice, UUID, Int) -> Unit
  ) {
    if(bluetoothGatt == null){
        return onFailed(device, charUUID, 1)
    }

    val service = bluetoothGatt?.getService(serviceUuid)
    val writeChar = service?.getCharacteristic(charUUID)
    writeChar?.value = data.toByteArray()
    writeChar?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    val status = bluetoothGatt?.writeCharacteristic(writeChar)

    if (status == false) {
      return onFailed(device, charUUID, 1)
    }

    this.onWriteSuccess = onSuccess
    this.onWriteFailed = onFailed
  }
}
