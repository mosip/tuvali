package com.ble.peripheral

import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import com.ble.peripheral.impl.Controller
import com.ble.peripheral.state.IMessageSender
import com.ble.peripheral.state.StateHandler
import com.ble.peripheral.state.message.AdvertisementStartMessage
import com.ble.peripheral.state.message.EnableCommunicationMessage
import com.ble.peripheral.state.message.SendDataMessage
import com.ble.peripheral.state.message.SetupGattServiceMessage
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
class Peripheral(context: Context, peripheralListener: IPeripheralListener) {
  private val logTag = "Peripheral"
  private val controller: Controller =
    Controller(context)
  private var messageSender: IMessageSender
  private val handlerThread: HandlerThread =
    HandlerThread("PeripheralHandlerThread", Process.THREAD_PRIORITY_DEFAULT)

  init {
    //TODO: Call quit once instance is done
    handlerThread.start()
    messageSender = StateHandler(handlerThread.looper, controller, peripheralListener)
    controller.setHandlerThread(messageSender)
  }

  fun setupService(service: BluetoothGattService) {
    val setupServiceMsg = SetupGattServiceMessage(service)
    messageSender.sendMessage(setupServiceMsg)
  }

  fun start(serviceUUID: UUID, scanRespUUID: UUID, advPayload: String, scanRespPayload: String) {
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

  fun sendData(serviceUUID: UUID, charUUID: UUID, data: UByteArray) {
    val currentState = messageSender.getCurrentState()
    if (currentState == StateHandler.States.CommunicationReady) {
      val sendDataMessage = SendDataMessage(serviceUUID, charUUID, data)
      messageSender.sendMessage(sendDataMessage)
    } else {
      Log.e(logTag, "sendData: failed as communication not ready, current state: $currentState")
    }
  }
}
