package com.ble.peripheral

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.IPeripheralListener
import com.ble.statemessage.AdvertisementStartFailureMessage
import com.ble.statemessage.AdvertisementStartMessage
import com.ble.statemessage.IMessage

class PeripheralStateHandler(
  looper: Looper,
  private val peripheralControllerDelegate: PeripheralControllerDelegate,
  private val peripheralListener: IPeripheralListener
) : Handler(looper) {
  private val logTag = "PeripheralHandlerThread"
  override fun handleMessage(msg: Message) {
    when (msg.what) {
      IMessage.PeripheralStates.ADV_START.ordinal -> {
        Log.d(logTag, "start advertisement")
        peripheralControllerDelegate.startAdvertisement(msg.obj as AdvertisementStartMessage)
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
}
