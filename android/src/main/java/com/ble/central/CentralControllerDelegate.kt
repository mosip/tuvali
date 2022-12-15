package com.ble.central

import android.content.Context
import com.ble.central.impl.GattClient
import com.ble.central.impl.Scanner
import com.ble.central.statemessage.*

import com.ble.peripheral.IPeripheralSendMessage

class CentralControllerDelegate(context: Context) {
  private var scanner: Scanner
  private var gattClient: GattClient
  private lateinit var messageSender: ICentralSendMessage

  init {
    gattClient = GattClient(context)
    scanner = Scanner(context)
    gattClient.init()
  }

  fun setHandlerThread(messageSender: ICentralSendMessage) {
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
