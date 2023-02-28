package io.mosip.tuvali.ble.peripheral.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.UUID
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

@SuppressLint("MissingPermission")
class GattServer(private val context: Context) : BluetoothGattServerCallback() {
  private val logTag = getLogTag((this::class.java.simpleName).toString())
  private lateinit var gattServer: BluetoothGattServer
  private var bluetoothDevice: BluetoothDevice? = null

  private lateinit var onMTUChangedCallback: (Int) -> Unit
  private lateinit var onServiceAddedCallback: (Int) -> Unit
  private lateinit var onDeviceConnectedCallback: (Int, Int) -> Unit
  private lateinit var onDeviceNotConnectedCallback: (Int, Int) -> Unit
  private lateinit var onReceivedWriteCallback: (BluetoothGattCharacteristic?, ByteArray?) -> Unit

  fun start(
    onDeviceConnected: (Int, Int) -> Unit,
    onDeviceNotConnected: (Int, Int) -> Unit,
    onReceivedWrite: (BluetoothGattCharacteristic?, ByteArray?) -> Unit,
    onMTUChanged: (Int)  -> Unit
  ) {
    onDeviceConnectedCallback = onDeviceConnected
    onDeviceNotConnectedCallback = onDeviceNotConnected
    onReceivedWriteCallback = onReceivedWrite
    onMTUChangedCallback = onMTUChanged
    val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    gattServer = bluetoothManager.openGattServer(context, this@GattServer)
    Log.i(logTag, "Device Address: ${bluetoothManager.adapter.address}")
  }

  fun close() {
    gattServer.close()
  }

  fun addService(service: BluetoothGattService, onServiceAdded: (Int) -> Unit) {
    onServiceAddedCallback = onServiceAdded
    gattServer.addService(service)
  }

  fun writeToChar(serviceUUID: UUID, charUUID: UUID, data: ByteArray): Boolean {
    if(bluetoothDevice == null) {
      Log.i(logTag, "Bluetooth device not available to write to char $charUUID")
      return false
    }
    val service = gattServer.getService(serviceUUID)
    val characteristic = service.getCharacteristic(charUUID)
    characteristic.value = data
    return gattServer.notifyCharacteristicChanged(bluetoothDevice, characteristic, false)
  }

  override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
    onServiceAddedCallback(status)
  }

  override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
    Log.i(logTag, "onConnectionStateChange: deviceAddress: ${device?.address}, status: $status," +
      " newState: $newState,  deviceHash: ${device.hashCode()}, deviceBondState: ${device?.bondState}")

    bluetoothDevice = if(newState == BluetoothProfile.STATE_CONNECTED){
      // Required by Android SDK to connect from peripheral side
      val connect = gattServer.connect(device, false)
      Log.i(logTag, "Connection initiated to central: $connect with device address: ${device?.address}")

      onDeviceConnectedCallback(status, newState)
      device?.let { setPhy(it) }
      device
    } else {
      Log.i(logTag,"Received disconnect from central with device address: ${device?.address}")
      onDeviceNotConnectedCallback(status, newState)
      null
    }
  }

  override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
    Log.d(logTag, "onMtuChanged: mtu: $mtu, device: $device")
    onMTUChangedCallback(mtu)
  }

  private fun setPhy(bluetoothDevice: BluetoothDevice) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      gattServer.readPhy(bluetoothDevice)
      gattServer.setPreferredPhy(bluetoothDevice, 2, 2, 0)
      gattServer.readPhy(bluetoothDevice)
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
    // Log.d(logTag, "onCharacteristicWriteRequest: requestId: ${requestId}, preparedWrite: ${preparedWrite}, responseNeeded: ${responseNeeded}, offset: ${offset}, dataSize: ${value?.size}")
    onReceivedWriteCallback(characteristic, value)
    if (responseNeeded) {
      val response = gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
      Log.d(logTag, "onCharacteristicWriteRequest: didResponseSent: ${response}")
    }
  }

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
    Log.d(logTag, "onCharacteristicReadRequest: isSuccessful: ${isSuccessful}, uuid: ${characteristic?.uuid} and value: ${characteristic?.value} and value size: ${characteristic?.value?.size}")
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

  override fun onPhyRead(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
    Log.d(logTag, "Peripheral onPhyRead: txPhy: $txPhy, rxPhy: $rxPhy, status: $status")
  }

  override fun onPhyUpdate(device: BluetoothDevice?, txPhy: Int, rxPhy: Int, status: Int) {
    Log.d(logTag, "Peripheral onPhyUpdate: txPhy: $txPhy, rxPhy: $rxPhy, status: $status")
  }

  fun disconnect(): Boolean {
    return if(bluetoothDevice != null) {
      Log.d(logTag, "Disconnecting from central with device address: ${bluetoothDevice?.address}")
      gattServer.cancelConnection(bluetoothDevice)
      bluetoothDevice = null
      true
    } else {
      Log.i(logTag,"Bluetooth device not available to disconnect !!")
      false
    }
  }
}
