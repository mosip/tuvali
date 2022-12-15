package com.ble.peripheral

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.IPeripheralListener
import com.ble.peripheral.statemessage.AdvertisementStartFailureMessage
import com.ble.peripheral.statemessage.AdvertisementStartMessage
import com.ble.peripheral.statemessage.IPeripheralMessage

class PeripheralStateHandler(
  looper: Looper,
  private val peripheralControllerDelegate: PeripheralControllerDelegate,
  private val peripheralListener: IPeripheralListener
) : Handler(looper) {
  private val logTag = "PeripheralHandlerThread"
  override fun handleMessage(msg: Message) {
    when (msg.what) {
      IPeripheralMessage.PeripheralStates.ADV_START.ordinal -> {
        Log.d(logTag, "start advertisement")
        peripheralControllerDelegate.startAdvertisement(msg.obj as AdvertisementStartMessage)
      }
      IPeripheralMessage.PeripheralStates.ADV_START_SUCCESS.ordinal -> {
        Log.d(logTag, "advertisement started successfully")
        peripheralListener.onAdvertisementStartSuccessful()
      }
      IPeripheralMessage.PeripheralStates.ADV_START_FAILURE.ordinal -> {
        Log.d(logTag, "advertisement start failed")
        val failureMsg = msg.obj as AdvertisementStartFailureMessage
        peripheralListener.onAdvertisementStartFailed(failureMsg.errorCode)
      }
    }
  }
}
