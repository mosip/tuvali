package io.mosip.tuvali.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.content.Context
import android.util.Log
import io.mosip.tuvali.ble.central.state.IMessageSender
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import io.mosip.tuvali.ble.central.state.message.*
import io.mosip.tuvali.transfer.Util
import java.util.UUID

const val MTU_HEADER_SIZE = 3
const val REQUEST_MTU_TIME_OUT = -1

class Controller(val context: Context) {
  private val logTag = getLogTag(javaClass.simpleName)

  private var gattClient: GattClient? = null
  private var scanner: Scanner? = null
  private lateinit var messageSender: IMessageSender
  private var requestedMTUValue = -1
  private lateinit var mtuValues : Array<Int>
  private var isMTURequestCallbackReceived: Boolean = false

  //TODO: Move it to gatt client instance
  private var peripheralDevice: BluetoothDevice? = null;

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun scan(scanStartMessage: ScanStartMessage) {
    scanner = Scanner(context)
    scanner?.start(
      scanStartMessage.serviceUUID,
      this::onDeviceFound,
      this::onScanStartFailure
    )
  }

  fun stopScan() {
    scanner?.stopScan()
  }

  @SuppressLint("MissingPermission")
  fun connect(device: BluetoothDevice) {
    gattClient = GattClient(context)
    gattClient?.connect(device, this::onDeviceConnected, this::onDeviceDisconnected)
  }

  fun write(writeMessage: WriteMessage) {
    //TODO: handle no device case
    peripheralDevice?.let {
      gattClient?.write(
        it,
        writeMessage.serviceUUID,
        writeMessage.charUUID,
        writeMessage.data,
        this::onWriteSuccess,
        this::onWriteFailed
      )
    }
  }

  fun read(readMessage: ReadMessage) {
    gattClient?.read(readMessage.serviceUUID, readMessage.charUUID, this::onReadSuccess, this::onReadFailed)
  }

  fun subscribe(subscribeMessage: SubscribeMessage) {
    val subscribed = gattClient?.subscribe(
      subscribeMessage.serviceUUID,
      subscribeMessage.charUUID,
      this::onNotificationReceived
    )

    if (subscribed == true) {
      messageSender.sendMessage(SubscribeSuccessMessage(subscribeMessage.charUUID))
    } else {
      messageSender.sendMessage(SubscribeFailureMessage(subscribeMessage.charUUID,
        BluetoothGatt.GATT_FAILURE
      ))
    }
  }

  fun unsubscribe(unsubscribeMessage: UnsubscribeMessage) {
    val unsubscribe = gattClient?.unsubscribe(
      unsubscribeMessage.serviceUUID,
      unsubscribeMessage.charUUID,
    )

    if (unsubscribe == true) {
      messageSender.sendMessage(UnsubscribeSuccessMessage(unsubscribeMessage.charUUID))
    } else {
      messageSender.sendMessage(UnsubscribeFailureMessage(unsubscribeMessage.charUUID, BluetoothGatt.GATT_FAILURE))
    }
  }

  fun discoverServices() {
    gattClient?.discoverServices(this::onServicesDiscovered, this::onServiceDiscoveryFailure)
  }

  fun requestMTU(mtuValues: Array<Int>, delayTime: Long) {
    this.mtuValues = mtuValues
    for (mtu in mtuValues) {
      requestedMTUValue = mtu
      gattClient?.requestMtu(requestedMTUValue, this::onRequestMTUSuccess, this::onRequestMTUFailure)
      Util.sleepInRealTime(delayTime)
      if (isMTURequestCallbackReceived) {
        break
      }
    }
    if (!isMTURequestCallbackReceived) {
      requestedMTUValue = -1
      onRequestMTUFailure(REQUEST_MTU_TIME_OUT)
    }
  }


  fun disconnect(): Boolean {
    return if(gattClient != null) {
      gattClient!!.disconnect()
    } else {
      Log.i(logTag,"Bluetooth device not available to disconnect")
      false
    }
  }

  fun close() {
    gattClient?.close()
  }

  private fun onNotificationReceived(charUUID: UUID, data: ByteArray) {
    messageSender.sendMessage(NotificationReceivedMessage(charUUID, data))
  }

  private fun onRequestMTUSuccess(negotiatedMtu: Int) {
    if(requestedMTUValue == negotiatedMtu) {
      isMTURequestCallbackReceived = true
      messageSender.sendMessage(RequestMTUSuccessMessage(negotiatedMtu - MTU_HEADER_SIZE))
    }
  }

  private fun onRequestMTUFailure(errorCode: Int) {
    messageSender.sendMessage(RequestMTUFailureMessage(errorCode))
  }

  private fun onReadSuccess(charUUID: UUID, value: ByteArray?) {
    val readSuccessMessage = ReadSuccessMessage(charUUID, value)

    messageSender.sendMessage(readSuccessMessage)
  }

  private fun onReadFailed(charUUID: UUID?, errorCode: Int) {
    val failedMessage = ReadFailureMessage(charUUID, errorCode)

    messageSender.sendMessage(failedMessage)
  }


  private fun onWriteSuccess(device: BluetoothDevice?, charUUID: UUID) {
    val writeSuccessMessage = WriteSuccessMessage(device, charUUID)
    messageSender.sendMessage(writeSuccessMessage)
  }

  private fun onWriteFailed(device: BluetoothDevice?, charUUID: UUID, errorCode: Int) {
    val writeFailureMessage = WriteFailureMessage(device, charUUID, errorCode)

    messageSender.sendMessage(writeFailureMessage)
  }

  private fun onDeviceFound(scanResult: ScanResult) {
    val deviceFoundMessage = DeviceFoundMessage(scanResult.device, scanResult.scanRecord)
    messageSender.sendMessage(deviceFoundMessage)
  }

  private fun onDeviceConnected(device: BluetoothDevice) {
    peripheralDevice = device
    val deviceConnectedMessage = DeviceConnectedMessage(device)

    messageSender.sendMessage(deviceConnectedMessage)
  }

  private fun onDeviceDisconnected() {
    peripheralDevice = null
    val deviceDisconnectedMessage = DeviceDisconnectedMessage()
    messageSender.sendMessage(deviceDisconnectedMessage)
  }

  private fun onScanStartFailure(errorCode: Int) {
    val scanStartFailureMessage = ScanStartFailureMessage(errorCode)

    messageSender.sendMessage(scanStartFailureMessage)
  }

  private fun onServicesDiscovered(serviceUuids: List<UUID>) {
    messageSender.sendMessage(DiscoverServicesSuccessMessage(serviceUuids))
  }

  private fun onServiceDiscoveryFailure(errorCode: Int) {
    messageSender.sendMessage(DiscoverServicesFailureMessage(errorCode))
  }
}
