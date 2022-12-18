package com.ble.central.state

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.central.impl.Controller
import com.ble.central.ICentralListener
import com.ble.central.state.message.*

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
      IMessage.CentralStates.SCAN_START_FAILURE.ordinal -> {
        Log.d(logTag, "scan start failed")
        val failureMsg = msg.obj as ScanStartFailureMessage
        listener.onScanStartedFailed(failureMsg.errorCode)
      }
      IMessage.CentralStates.DEVICE_FOUND.ordinal -> {
        Log.d(logTag, "device found successfully with ${msg.obj}")
        Log.d(logTag, "device found successfully with ${msg.what}")
        val deviceFoundMessage = msg.obj as DeviceFoundMessage

        listener.onDeviceFound(deviceFoundMessage.device)
      }
      IMessage.CentralStates.CONNECT_DEVICE.ordinal -> {
        Log.d(logTag, "connect device successfully")
        val connectDeviceMessage = msg.obj as ConnectDeviceMessage

        controller.connect(connectDeviceMessage.device)
      }
      IMessage.CentralStates.DEVICE_CONNECTED.ordinal -> {
        Log.d(logTag, "device connected successfully")
        listener.onDeviceConnected()
      }
      IMessage.CentralStates.DEVICE_DISCONNECTED.ordinal -> {
        Log.d(logTag, "device disconnected successfully")
        listener.onDeviceDisconnected()
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
