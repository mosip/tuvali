package io.mosip.tuvali.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGatt.*
import android.content.Context
import android.util.Log
import java.util.*
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import kotlin.math.min

//Set maximum attribute value as defined by spec Core 5.3
//https://github.com/dariuszseweryn/RxAndroidBle/pull/808
private const val MAX_ALLOWED_MTU_VALUE = 512
class GattClient(var context: Context) {
  private var onNotificationReceived: ((UUID, ByteArray) -> Unit)? = null
  private lateinit var onReadSuccess: (UUID, ByteArray?) -> Unit
  private lateinit var onReadFailure: (UUID?, Int) -> Unit
  private lateinit var onRequestMTUSuccess: (mtu: Int) -> Unit
  private lateinit var onRequestMTUFailure: (err: Int) -> Unit
  private lateinit var onServicesDiscoveryFailure: (err: Int) -> Unit
  private lateinit var onServicesDiscovered: (List<UUID>) -> Unit
  private lateinit var onWriteFailed: (BluetoothDevice?, UUID, Int) -> Unit
  private lateinit var onWriteSuccess: (BluetoothDevice?, UUID) -> Unit
  private lateinit var onDeviceDisconnected: () -> Unit
  private lateinit var onDeviceConnected: (BluetoothDevice) -> Unit
  private var peripheral: BluetoothDevice? = null
  private var bluetoothGatt: BluetoothGatt? = null
  private val logTag = getLogTag(javaClass.simpleName)
  private var tempCounterMap = mutableMapOf<UUID, Int>()

  private val bluetoothGattCallback = object : BluetoothGattCallback() {
    override fun onCharacteristicWrite(
      gatt: BluetoothGatt?,
      characteristic: BluetoothGattCharacteristic?,
      status: Int
    ) {
      if (characteristic?.uuid != null) {
        if (tempCounterMap[characteristic.uuid!!] == null) {
          tempCounterMap[characteristic.uuid!!] = 0
        } else {
          tempCounterMap[characteristic.uuid!!] = (tempCounterMap[characteristic.uuid] as Int) + 1
        }
      }
      Log.d(logTag, "Status of write is $status for ${characteristic?.uuid}, tempWriteCounterForCharUUID: ${tempCounterMap[characteristic?.uuid]}")

      if (status != GATT_SUCCESS) {
        Log.i(logTag, "Failed to send message to peripheral")

        characteristic?.uuid?.let { uuid ->
          onWriteFailed(peripheral, uuid, status)
        }
        return
      }

      characteristic?.let {
        onWriteSuccess(peripheral,it.uuid)
      }
    }

    override fun onCharacteristicRead(
      gatt: BluetoothGatt?,
      characteristic: BluetoothGattCharacteristic?,
      status: Int
    ) {
      if (status == GATT_SUCCESS && characteristic != null) {
        onReadSuccess(characteristic.uuid, characteristic.value)
        Log.i(logTag, "Successfully read char: : ${characteristic.uuid}")
      } else {
        onReadFailure(characteristic?.uuid, status)
        Log.i(logTag, "Failed to read char: : ${characteristic?.uuid}")
      }
    }

    override fun onCharacteristicChanged(
      gatt: BluetoothGatt?,
      characteristic: BluetoothGattCharacteristic?
    ) {
      Log.i(
        logTag, "Got notification from char ${characteristic?.uuid} and value ${
          characteristic?.value?.get(0)?.toInt()
        }"
      )

      characteristic?.let {
        if (onNotificationReceived != null) {
          onNotificationReceived?.let { it1 -> it1(it.uuid, it.value) }
        } else {
          Log.d(logTag, "Notification receiver callback is not set")
        }
      }
    }

    override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
      super.onMtuChanged(gatt, mtu, status)
      val allowedMTU = min(mtu, MAX_ALLOWED_MTU_VALUE)

      if (status == GATT_SUCCESS) {
        onRequestMTUSuccess(allowedMTU)
        Log.i(logTag, "Successfully changed mtu size: $allowedMTU")
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
      val serviceUuids = gatt?.services?.map { it.uuid }
      if (serviceUuids != null) {
        onServicesDiscovered(serviceUuids)
      } else {
        onServicesDiscovered(emptyList())
      }
    }

    @SuppressLint("MissingPermission")
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
      if (newState == BluetoothProfile.STATE_CONNECTED) {
        Log.i(logTag, "Connected to the peripheral")
        peripheral?.let { onDeviceConnected(it) }

      } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        Log.i(logTag, "Disconnected from the peripheral with address: ${gatt?.device?.address}")
        peripheral?.let{ onDeviceDisconnected() }

        peripheral = null;
      }else {
        Log.w(logTag, "Received connection state change with status: $status, newState: $newState, device address: ${gatt?.device?.address}")
      }
    }
  }

  @SuppressLint("MissingPermission", "NewApi")
  fun connect(
    device: BluetoothDevice,
    onDeviceConnected: (BluetoothDevice) -> Unit,
    onDeviceDisconnected: () -> Unit
  ) {
    Log.i(logTag, "Initiating connect to ble peripheral")

    if(peripheral != null) {
      Log.i(logTag, "Device is already connected.")
      return
    }

    this.onDeviceConnected = onDeviceConnected;
    this.onDeviceDisconnected = onDeviceDisconnected;
    peripheral = device;

    val gatt = device.connectGatt(
      context,
      false,
      bluetoothGattCallback,
      BluetoothDevice.TRANSPORT_LE
    )
    // TODO: In case device is never connected, how do we know?

    gatt.requestConnectionPriority(CONNECTION_PRIORITY_HIGH)
    gatt.readPhy()
    this.bluetoothGatt = gatt
  }

  @SuppressLint("MissingPermission")
  fun write(
    device: BluetoothDevice,
    serviceUuid: UUID,
    charUUID: UUID,
    data: ByteArray,
    onSuccess: (BluetoothDevice?, UUID) -> Unit,
    onFailed: (BluetoothDevice?, UUID, Int) -> Unit
  ) {
    if (bluetoothGatt == null) {
      return onFailed(device, charUUID, GATT_FAILURE)
    }
    Log.i(logTag, "Initiating write to peripheral char: $charUUID")

    val service = bluetoothGatt?.getService(serviceUuid)
    val writeChar = service?.getCharacteristic(charUUID)
    writeChar?.value = data
    writeChar?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    this.onWriteSuccess = onSuccess
    this.onWriteFailed = onFailed
    val status = bluetoothGatt?.writeCharacteristic(writeChar)

    if (status == false) {
      return onFailed(device, charUUID, GATT_FAILURE)
    }
  }

  @SuppressLint("MissingPermission")
  fun discoverServices(onSuccess: (List<UUID>) -> Unit, onFailure: (err: Int) -> Unit) {
    this.onServicesDiscovered = onSuccess
    this.onServicesDiscoveryFailure = onFailure
    bluetoothGatt?.discoverServices()
  }

  @SuppressLint("MissingPermission")
  fun requestMtu(mtu: Int, onSuccess: (mtu: Int) -> Unit, onFailure: (err: Int) -> Unit) {
    Log.d(logTag, "Request mtu change to $mtu")
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
  ) {
    this.onReadSuccess = onSuccess
    this.onReadFailure = onFailure

    try {
      val service = bluetoothGatt!!.getService(serviceUUID)
      val characteristic = service.getCharacteristic(charUUID)
      val read = bluetoothGatt!!.readCharacteristic(characteristic)

      if (!read) {
        Log.d(logTag, "Failed to start reading")
        onFailure(charUUID, GATT_FAILURE)
      }

    } catch (e: Error) {
      onFailure(charUUID, GATT_FAILURE)
    }
  }

  @SuppressLint("MissingPermission")
  fun subscribe(
    serviceUUID: UUID,
    charUUID: UUID,
    onNotificationReceived: (UUID, ByteArray) -> Unit
  ): Boolean {
    this.onNotificationReceived = onNotificationReceived

    try {
      val service = bluetoothGatt!!.getService(serviceUUID)
      val characteristic = service.getCharacteristic(charUUID)
      val notificationsEnabled = bluetoothGatt!!.setCharacteristicNotification(characteristic, true)

      return if (notificationsEnabled) {
        true
      } else {
        Log.d(logTag, "Failed to subscribe to $charUUID")
        false
      }

    } catch (e: Error) {
      return false
    }
  }


  @SuppressLint("MissingPermission")
  fun unsubscribe(
    serviceUUID: UUID,
    charUUID: UUID,
  ): Boolean {

    try {
      val service = bluetoothGatt!!.getService(serviceUUID)
      val characteristic = service.getCharacteristic(charUUID)
      val notificationsEnabled = bluetoothGatt!!.setCharacteristicNotification(characteristic, false)
      onNotificationReceived = null

      return if (notificationsEnabled) {
        true
      } else {
        Log.d(logTag, "Failed to unsubscribe to $charUUID")
        false
      }

    } catch (e: Error) {
      return false
    }
  }

  @SuppressLint("MissingPermission")
  fun disconnect(): Boolean {
    return if (bluetoothGatt != null && peripheral != null) {
      Log.i(logTag, "Disconnecting connection")
      bluetoothGatt!!.disconnect()
      true
    } else {
      Log.i(logTag, "Ignoring disconnect request")
      false
    }
  }

  @SuppressLint("MissingPermission")
  fun close() {
    if (bluetoothGatt != null) {
      Log.i(logTag, "Closing Bluetooth client")
      bluetoothGatt!!.close()
      bluetoothGatt = null
    } else {
      Log.i(logTag, "Ignoring close Bluetooth client request")
    }
  }

}
