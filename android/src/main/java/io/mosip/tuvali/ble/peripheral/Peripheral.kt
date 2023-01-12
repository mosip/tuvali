package io.mosip.tuvali.ble.peripheral

import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import io.mosip.tuvali.ble.peripheral.impl.Controller
import io.mosip.tuvali.ble.peripheral.state.IMessageSender
import io.mosip.tuvali.ble.peripheral.state.StateHandler
import io.mosip.tuvali.ble.peripheral.state.message.*
import java.util.*

class Peripheral(context: Context, peripheralListener: IPeripheralListener) {
  private val logTag = "Peripheral"
  private val controller: Controller =
    Controller(context)
  private var messageSender: IMessageSender
  private val handlerThread: HandlerThread =
    HandlerThread("PeripheralHandlerThread", Process.THREAD_PRIORITY_DEFAULT)

  init {
    handlerThread.start()
    messageSender = StateHandler(handlerThread.looper, controller, peripheralListener)
    controller.setHandlerThread(messageSender)
  }

  fun stop() {
    stopAdvertisement()
    disconnectAndClose()
  }

  fun setupService(service: BluetoothGattService) {
    val setupServiceMsg = SetupGattServiceMessage(service)
    messageSender.sendMessage(setupServiceMsg)
  }

  fun start(serviceUUID: UUID, scanRespUUID: UUID, advPayload: ByteArray, scanRespPayload: ByteArray) {
    val advStartMsg = AdvertisementStartMessage(
      serviceUUID,
      scanRespUUID,
      advPayload,
      scanRespPayload
    )
    messageSender.sendMessage(advStartMsg)
  }

  fun enableCommunication() {
    val enabledCommMsg = EnableCommunicationMessage()
    messageSender.sendMessage(enabledCommMsg)
  }

  fun sendData(serviceUUID: UUID, charUUID: UUID, data: ByteArray) {
    val currentState = messageSender.getCurrentState()
    if (currentState == StateHandler.States.CommunicationReady) {
      val sendDataMessage = SendDataMessage(serviceUUID, charUUID, data)
      messageSender.sendMessage(sendDataMessage)
    } else {
      Log.e(logTag, "sendData: failed as communication not ready, current state: $currentState")
    }
  }

  fun disconnect() {
   messageSender.sendMessage(DisconnectDeviceMessage())
  }

  fun close() {
    messageSender.sendMessage(CloseServerMessage())
  }

  fun stopAdvertisement() {
    messageSender.sendMessage(AdvertisementStopMessage())
  }

  fun quitHandler() {
    handlerThread.quitSafely()
  }

  fun disconnectAndClose() {
    messageSender.sendMessage(DisconnectAndCloseMessage())
  }
}
