package com.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import com.ble.central.state.IMessageSender
import com.ble.central.state.message.*
import java.util.UUID

class Controller(context: Context) {
  private var scanner: Scanner
  private var gattClient: GattClient
  private lateinit var messageSender: IMessageSender
  private var peripheralDevice: BluetoothDevice? = null;

  init {
    gattClient = GattClient(context)
    scanner = Scanner(context)
  }

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun scan(scanStartMessage: ScanStartMessage) {
    scanner.start(
      scanStartMessage.serviceUUID,
      scanStartMessage.advPayload,
      this::onDeviceFound,
      this::onScanStartFailure
    )
  }

  @SuppressLint("MissingPermission")
  fun connect(device: BluetoothDevice) {
    gattClient.connect(device, this::onDeviceConnected, this::onDeviceDisconnected)
  }

  fun write(writeMessage: WriteMessage) {
    //TODO: handle no device case
    peripheralDevice?.let {
      gattClient.write(it, writeMessage.serviceUUID, writeMessage.charUUID, writeMessage.data, this::onWriteSuccess, this::onWriteFailed)
    }
  }

  private fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID) {
    val writeSuccessMessage = WriteSuccessMessage(device, charUUID)

    messageSender.sendMessage(writeSuccessMessage)
  }

  private fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, errorCode: Int) {
    val writeFailedMessage = WriteFailedMessage(device, charUUID, errorCode)

    messageSender.sendMessage(writeFailedMessage)
  }

  private fun onDeviceFound(device: BluetoothDevice) {
    val deviceFoundMessage = DeviceFoundMessage(device)
    messageSender.sendMessage(deviceFoundMessage)
  }

  private fun onDeviceConnected(device: BluetoothDevice) {
    peripheralDevice = device
    val deviceConnectedMessage = DeviceConnectedMessage(device)

    messageSender.sendMessage(deviceConnectedMessage)
  }

  private fun onDeviceDisconnected(device: BluetoothDevice) {
    peripheralDevice = null
    val deviceDisconnectedMessage = DeviceDisconnectedMessage(device)
    messageSender.sendMessage(deviceDisconnectedMessage)
  }

  private fun onScanStartFailure(errorCode: Int) {
    val scanStartFailureMessage = ScanStartFailureMessage(errorCode)
    messageSender.sendMessage(scanStartFailureMessage)
  }

}
