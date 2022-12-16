package com.ble.central.impl

import android.content.Context
import com.ble.central.state.IMessageSender
import com.ble.central.state.message.ScanStartFailureMessage
import com.ble.central.state.message.ScanStartMessage
import com.ble.central.state.message.ScanStartSuccessMessage

class Controller(context: Context) {
  private var scanner: Scanner
  private var gattClient: GattClient
  private lateinit var messageSender: IMessageSender

  init {
    gattClient = GattClient(context)
    scanner = Scanner(context)
    gattClient.init()
  }

  fun setHandlerThread(messageSender: IMessageSender) {
    this.messageSender = messageSender
  }

  fun scan(scanStartMessage: ScanStartMessage) {
    scanner.start(
      scanStartMessage.serviceUUID,
      scanStartMessage.scanRespUUID,
      scanStartMessage.advPayload,
      this::onScanStartSuccess,
      this::onScanStartFailure
    )
  }

  private fun onScanStartSuccess() {
    val scanStartSuccessMessage = ScanStartSuccessMessage()
    messageSender.sendMessage(scanStartSuccessMessage)
  }

  private fun onScanStartFailure(errorCode: Int) {
    val scanStartFailureMessage = ScanStartFailureMessage(errorCode)
    messageSender.sendMessage(scanStartFailureMessage)
  }
}
