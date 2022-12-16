package com.ble.central.state

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.central.impl.Controller
import com.ble.central.ICentralListener
import com.ble.central.state.message.IMessage
import com.ble.central.state.message.ScanStartFailureMessage
import com.ble.central.state.message.ScanStartMessage

class StateHandler(
  looper: Looper,
  private val controller: Controller,
  private val listener: ICentralListener
) : Handler(looper), IMessageSender {
  private val logTag = "CentralHandlerThread"

  override fun handleMessage(msg: Message) {
    when (msg.what) {
      IMessage.CentralStates.SCAN_START.ordinal -> {
        Log.d(logTag, "start scan")
        controller.scan(msg.obj as ScanStartMessage)
      }
      IMessage.CentralStates.SCAN_START_SUCCESS.ordinal -> {
        Log.d(logTag, "scan started successfully")
        listener.onScanStartedSuccessfully()
      }
      IMessage.CentralStates.SCAN_START_FAILURE.ordinal -> {
        Log.d(logTag, "scan start failed")
        val failureMsg = msg.obj as ScanStartFailureMessage
        listener.onScanStartedFailed(failureMsg.errorCode)
      }
    }
  }

  override fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.commandType.ordinal
    message.obj = msg
    this.sendMessage(message)
  }
}
