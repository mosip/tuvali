package com.ble.peripheral.state

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.peripheral.IPeripheralListener
import com.ble.peripheral.impl.Controller
import com.ble.peripheral.state.message.AdvertisementStartFailureMessage
import com.ble.peripheral.state.message.AdvertisementStartMessage
import com.ble.peripheral.state.message.IMessage

class StateHandler(
  looper: Looper,
  private val controller: Controller,
  private val peripheralListener: IPeripheralListener
) : Handler(looper), IMessageSender {
  private val logTag = "PeripheralHandlerThread"
  override fun handleMessage(msg: Message) {
    when (msg.what) {
      IMessage.PeripheralStates.ADV_START.ordinal -> {
        Log.d(logTag, "start advertisement")
        controller.startAdvertisement(msg.obj as AdvertisementStartMessage)
      }
      IMessage.PeripheralStates.ADV_START_SUCCESS.ordinal -> {
        Log.d(logTag, "advertisement started successfully")
        peripheralListener.onAdvertisementStartSuccessful()
      }
      IMessage.PeripheralStates.ADV_START_FAILURE.ordinal -> {
        Log.d(logTag, "advertisement start failed")
        val failureMsg = msg.obj as AdvertisementStartFailureMessage
        peripheralListener.onAdvertisementStartFailed(failureMsg.errorCode)
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
