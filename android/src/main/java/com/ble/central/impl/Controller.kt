package com.ble.central.impl

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.ble.central.state.IMessageSender
import com.ble.central.state.message.*
import com.openid4vpble.Openid4vpBleModule
import java.util.UUID

class Controller(context: Context) {
  private var scanner: Scanner
  private var gattClient: GattClient
  private lateinit var messageSender: IMessageSender

  init {
    gattClient = GattClient(context)
    scanner = Scanner(context)
  }

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun scan(scanStartMessage: ScanStartMessage) {
    Log.d(Openid4vpBleModule.LOG_TAG, "BLE: starting scan")

    scanner.start(
      scanStartMessage.serviceUUID,
      scanStartMessage.advPayload,
      this::onDeviceFound,
      this::onScanStartFailure
    )
  }

  @SuppressLint("MissingPermission")
  fun connect(device: BluetoothDevice) {
    Log.d(Openid4vpBleModule.LOG_TAG, "BLE: Connecting to device: ${device.name}", )

    gattClient.connect(device, this::onDeviceConnected, this::onDeviceDisconnected)
  }

  fun write(writeMessage: WriteMessage) {
    gattClient.write(writeMessage.device, writeMessage.serviceUUID, writeMessage.charUUID, writeMessage.data, this::onWriteSuccess, this::onWriteFailed)
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
    Log.d(Openid4vpBleModule.LOG_TAG, "Sent message to on device found" )

    messageSender.sendMessage(deviceFoundMessage)
  }

  private fun onDeviceConnected(device: BluetoothDevice) {
    val deviceConnectedMessage = DeviceConnectedMessage(device)

    messageSender.sendMessage(deviceConnectedMessage)
  }

  private fun onDeviceDisconnected(device: BluetoothDevice) {
    val deviceDisconnectedMessage = DeviceDisconnectedMessage(device)
    messageSender.sendMessage(deviceDisconnectedMessage)
  }

  private fun onScanStartFailure(errorCode: Int) {
    val scanStartFailureMessage = ScanStartFailureMessage(errorCode)
    messageSender.sendMessage(scanStartFailureMessage)
  }

}
