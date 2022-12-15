package com.ble

import android.content.Context
import com.ble.peripheral.IPeripheralSendMessage
import com.ble.peripheral.PeripheralControllerDelegate
import com.ble.peripheral.PeripheralStateHandlerThread
import com.ble.peripheral.statemessage.AdvertisementStartMessage
import java.util.*

class Peripheral(context: Context, peripheralListener: IPeripheralListener) {
  private val peripheralControllerDelegate: PeripheralControllerDelegate = PeripheralControllerDelegate(context)
  private val messageSender: IPeripheralSendMessage = PeripheralStateHandlerThread(peripheralControllerDelegate, peripheralListener)

  init {
      peripheralControllerDelegate.setHandlerThread(messageSender)
  }

  fun start(serviceUUID: UUID, scanRespUUID: UUID, advPayload: String,  scanRespPayload: String) {
    val advStartCmd = AdvertisementStartMessage(
      serviceUUID,
      scanRespUUID,
      advPayload,
      scanRespPayload
    )
    messageSender.sendMessage(advStartCmd)
  }
}
