package io.mosip.tuvali.ble.peripheral.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import io.mosip.tuvali.common.BluetoothStateChangeReceiver
import java.util.UUID
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import kotlin.math.min

//Set maximum attribute value as defined by spec Core 5.3
//https://github.com/dariuszseweryn/RxAndroidBle/pull/808
private const val MAX_ALLOWED_MTU_VALUE = 512

@SuppressLint("MissingPermission")
class GattServer(private val context: Context) : BluetoothGattServerCallback() {
  private val logTag = getLogTag(javaClass.simpleName)
  private lateinit var gattServer: BluetoothGattServer
  private var bluetoothDevice: BluetoothDevice? = null

  private lateinit var onMTUChangedCallback: (Int) -> Unit
  private lateinit var onServiceAddedCallback: (Int) -> Unit
  private lateinit var onDeviceConnectedCallback: (Int, Int) -> Unit
  private lateinit var onDeviceNotConnectedCallback: (Int, Int) -> Unit
  private lateinit var onReceivedWriteCallback: (BluetoothGattCharacteristic?, ByteArray?) -> Unit
  private lateinit var bluetoothStateChangeReceiver: BluetoothStateChangeReceiver
  private var bluetoothStateChangeIntentFilter: IntentFilter = IntentFilter()

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

    bluetoothStateChangeReceiver = BluetoothStateChangeReceiver(onDeviceNotConnected)

    bluetoothStateChangeIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    context.registerReceiver(bluetoothStateChangeReceiver, bluetoothStateChangeIntentFilter)
    Log.i(logTag, "Device Address: ${bluetoothManager.adapter.address}")
  }

  fun close() {
    context.unregisterReceiver(bluetoothStateChangeReceiver)
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
    //Calling getServices() to fix issue of empty services during service discovery.
    Log.d(logTag, "List of services: ${gattServer.services.map { it.uuid }}")
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
      device
    } else {
      Log.i(logTag,"Received disconnect from central with device address: ${device?.address}")
      onDeviceNotConnectedCallback(status, newState)
      null
    }
  }

  override fun onMtuChanged(device: BluetoothDevice?, mtu: Int) {
    Log.d(logTag, "onMtuChanged: mtu: $mtu, device: $device")
    val allowedMTU = min( mtu, MAX_ALLOWED_MTU_VALUE)
    Log.d(logTag, "successfully changed mtu to $allowedMTU")
    onMTUChangedCallback(allowedMTU)
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
      gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
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
