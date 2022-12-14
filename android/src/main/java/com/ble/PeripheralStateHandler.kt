package com.ble

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.handlercommand.AdvertisementCommand
import com.ble.handlercommand.ICommand

class PeripheralStateHandler(looper: Looper, private val peripheralHandlerDelegate: PeripheralHandlerDelegate) : Handler(looper) {
  private val logTag = "PeripheralHandlerThread"
  override fun handleMessage(msg: Message) {
    when (msg.what) {
      ICommand.PeripheralStates.ADV_START.ordinal -> {
          Log.d(logTag, "start advertisement")
          peripheralHandlerDelegate.startAdvertisement(msg.obj as AdvertisementCommand)
      }
      ICommand.PeripheralStates.ADV_START_SUCCESS.ordinal -> Log.d(
          logTag,
          "advertisement started successfully"
      )
      ICommand.PeripheralStates.ADV_START_FAILURE.ordinal -> Log.d(
          logTag,
          "advertisement start failed"
      )
    }
  }
}
