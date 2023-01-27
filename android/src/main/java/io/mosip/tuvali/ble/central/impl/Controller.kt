package io.mosip.tuvali.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.le.ScanResult
import android.content.Context
import io.mosip.tuvali.ble.central.state.IMessageSender
import io.mosip.tuvali.ble.central.state.message.*
import java.util.UUID

class Controller(val context: Context) {
  private lateinit var scanner: Scanner
  private var gattClient: GattClient? = null
  private lateinit var messageSender: IMessageSender

  //TODO: Move it to gatt client instance
  private var peripheralDevice: BluetoothDevice? = null;

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun scan(scanStartMessage: ScanStartMessage) {
    scanner = Scanner(context)
    scanner.start(
      scanStartMessage.serviceUUID,
      scanStartMessage.advPayload,
      this::onDeviceFound,
      this::onScanStartFailure
    )
  }

  fun stopScan() {
    scanner.stopScan()
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

  fun requestMTU(mtu: Int) {
    gattClient?.requestMtu(mtu, this::onRequestMTUSuccess, this::onRequestMTUFailure)
  }

  fun disconnect(): Boolean {
    return if(gattClient != null) {
      gattClient!!.disconnect()
    } else false
  }

  fun close() {
    gattClient?.close()
  }

  private fun onNotificationReceived(charUUID: UUID, data: ByteArray) {
    messageSender.sendMessage(NotificationReceivedMessage(charUUID, data))
  }

  private fun onRequestMTUSuccess(mtu: Int) {
    messageSender.sendMessage(RequestMTUSuccessMessage(mtu))
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

  private fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID) {
    val writeSuccessMessage = WriteSuccessMessage(device, charUUID)

    messageSender.sendMessage(writeSuccessMessage)
  }

  private fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, errorCode: Int) {
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
