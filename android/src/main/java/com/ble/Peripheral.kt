package com.ble

import android.content.Context
import com.ble.handlercommand.AdvertisementCommand
import com.ble.handlercommand.ICommand
import java.util.*

class Peripheral(context: Context) {
  private val peripheralHandlerDelegate: PeripheralHandlerDelegate = PeripheralHandlerDelegate(context)
  private val handlerThread: PeripheralHandlerThread = PeripheralHandlerThread(peripheralHandlerDelegate)

  fun start(serviceUUID: UUID, scanRespUUID: UUID, advPayload: String,  scanRespPayload: String) {
    val advStartCmd = AdvertisementCommand(
      ICommand.PeripheralStates.ADV_START,
      serviceUUID,
      scanRespUUID,
      advPayload,
      scanRespPayload
    )
    handlerThread.sendMessage(advStartCmd)
  }
}
