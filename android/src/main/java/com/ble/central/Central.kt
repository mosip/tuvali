package com.ble.central

import android.content.Context
import android.os.HandlerThread
import android.os.Process
import com.ble.central.impl.Controller
import com.ble.central.state.IMessageSender
import com.ble.central.state.message.ScanStartMessage
import com.ble.central.state.StateHandler
import java.util.*

class Central(context: Context, centralLister: ICentralListener) {
  private val controller: Controller = Controller(context)
  private val handlerThread = HandlerThread("CentralHandlerThread", Process.THREAD_PRIORITY_DEFAULT)
  private var messageSender: IMessageSender

  init {
    handlerThread.start()
    messageSender = StateHandler(handlerThread.looper, controller, centralLister)
    controller.setHandlerThread(messageSender)
  }

  fun scan(serviceUuid: UUID, scanResponseServiceUuid: UUID, advIdentifier: String) {
    val scanStartMessage = ScanStartMessage(serviceUuid, scanResponseServiceUuid, advIdentifier)

    messageSender.sendMessage(scanStartMessage)
  }

}
