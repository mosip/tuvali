package com.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_FAILURE
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.content.Context
import android.util.Log
import java.util.*

class GattClient(var context: Context) {
  private lateinit var onReadSuccess: (UUID, ByteArray?) -> Unit
  private lateinit var onReadFailure: (UUID?, Int) -> Unit
  private lateinit var onRequestMTUSuccess: (mtu: Int) -> Unit
  private lateinit var onRequestMTUFailure: (err: Int) -> Unit
  private lateinit var onServicesDiscoveryFailure: (err: Int) -> Unit
  private lateinit var onServicesDiscovered: () -> Unit
  private lateinit var onWriteFailed: (BluetoothDevice, UUID, Int) -> Unit
  private lateinit var onWriteSuccess: (BluetoothDevice, UUID) -> Unit
  private lateinit var onDeviceDisconnected: () -> Unit
  private lateinit var onDeviceConnected: (BluetoothDevice) -> Unit;
  private var peripheral: BluetoothDevice? = null;
  private var bluetoothGatt: BluetoothGatt? = null;
  private val logTag = "BLECentral"

  private val bluetoothGattCallback = object : BluetoothGattCallback() {
    override fun onCharacteristicWrite(
      gatt: BluetoothGatt?,
      characteristic: BluetoothGattCharacteristic?,
      status: Int
    ) {
      Log.i(logTag, "Status of write is $status for ${characteristic?.uuid}")

      if(status != GATT_SUCCESS) {
        Log.i(logTag, "\"Failed to send message to peripheral")

        peripheral?.let {
          characteristic?.uuid?.let {
              uuid -> onWriteFailed(it, uuid, status)
          } }

        return
      }

      peripheral?.let {
        characteristic?.let {
            char -> onWriteSuccess(it, char.uuid) }
      }
    }

    override fun onCharacteristicRead(
      gatt: BluetoothGatt?,
      characteristic: BluetoothGattCharacteristic?,
      status: Int
    ) {
      super.onCharacteristicRead(gatt, characteristic, status)

      if(status == GATT_SUCCESS && characteristic != null) {
        onReadSuccess(characteristic.uuid, characteristic.value)
        Log.i(logTag, "Successfully read char: : ${characteristic.uuid}")
      } else {
        onReadFailure(characteristic?.uuid, status)
        Log.i(logTag, "Failed to read char: : ${characteristic?.uuid}")
      }
    }
    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
      super.onMtuChanged(gatt, mtu, status)

      if(status == GATT_SUCCESS) {
        onRequestMTUSuccess(mtu)
        Log.i(logTag, "Successfully changed mtu size: $mtu")
      } else {
        onRequestMTUFailure(status)
        Log.i(logTag, "Failed to change mtu due to ErrorCode: $status")
      }

    }

    @SuppressLint("MissingPermission")
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
      super.onServicesDiscovered(gatt, status)
      if (status != GATT_SUCCESS) {
        Log.e(logTag, "Failed to discover services")
        onServicesDiscoveryFailure(status)
        return
      }

      Log.i(logTag, "discovered services: ${gatt?.services?.map { it.uuid }}")
      onServicesDiscovered()
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
      if (newState == BluetoothProfile.STATE_CONNECTED) {
        Log.i(logTag, "Connected to the peripheral")
        peripheral?.let { onDeviceConnected(it) }

      } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        Log.i(logTag, "Disconnected from the peripheral")
        closeGatt()

        peripheral = null;
      }
    }
  }

  @SuppressLint("MissingPermission")
  private fun closeGatt() {
    bluetoothGatt?.close()
    peripheral?.let { onDeviceDisconnected() };

    bluetoothGatt = null

  }

  @SuppressLint("MissingPermission", "NewApi")
  fun connect(
    device: BluetoothDevice,
    onDeviceConnected: (BluetoothDevice) -> Unit,
    onDeviceDisconnected: () -> Unit
  ) {
    Log.i(logTag, "Initiating connect to ble peripheral")

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
    data: ByteArray,
    onSuccess: (BluetoothDevice, UUID) -> Unit,
    onFailed: (BluetoothDevice, UUID, Int) -> Unit
  ) {
    if(bluetoothGatt == null){
        return onFailed(device, charUUID, GATT_FAILURE)
    }
    Log.i(logTag, "Initiating write to peripheral char: $charUUID")

    val service = bluetoothGatt?.getService(serviceUuid)
    val writeChar = service?.getCharacteristic(charUUID)
    writeChar?.value = data
    writeChar?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    val status = bluetoothGatt?.writeCharacteristic(writeChar)

    if (status == false) {
      return onFailed(device, charUUID, GATT_FAILURE)
    }

    this.onWriteSuccess = onSuccess
    this.onWriteFailed = onFailed
  }

  @SuppressLint("MissingPermission")
  fun discoverServices(onSuccess: () -> Unit, onFailure: (err: Int) -> Unit) {
    this.onServicesDiscovered = onSuccess
    this.onServicesDiscoveryFailure = onFailure
    bluetoothGatt?.discoverServices()
  }

  @SuppressLint("MissingPermission")
  fun requestMtu(mtu: Int, onSuccess: (mtu: Int) -> Unit, onFailure: (err: Int) -> Unit) {
    val success = bluetoothGatt?.requestMtu(mtu)
    this.onRequestMTUSuccess = onSuccess
    this.onRequestMTUFailure = onFailure

    if (success == false) {
      Log.i(logTag, "Failed to request MTU change")
      onFailure(GATT_FAILURE)
    }

  }

  @SuppressLint("MissingPermission")
  fun read(
    serviceUUID: UUID,
    charUUID: UUID,
    onSuccess: (UUID, ByteArray?) -> Unit,
    onFailure: (UUID?, Int) -> Unit
  ): Boolean {
    this.onReadSuccess = onSuccess
    this.onReadFailure = onFailure

    try {
      val service = bluetoothGatt!!.getService(serviceUUID)
      val characteristic = service.getCharacteristic(charUUID)
      bluetoothGatt!!.readCharacteristic(characteristic)
    } catch(e: Error) {
      return false
    }

    return true
  }
}
