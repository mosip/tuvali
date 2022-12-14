package com.ble

import android.content.Context
import com.ble.peripheral.ISendMessage
import com.ble.peripheral.PeripheralControllerDelegate
import com.ble.peripheral.PeripheralStateHandlerThread
import com.ble.statemessage.AdvertisementStartMessage
import com.ble.statemessage.IMessage
import java.util.*

class Peripheral(context: Context, peripheralListener: IPeripheralListener) {
  private val peripheralControllerDelegate: PeripheralControllerDelegate = PeripheralControllerDelegate(context)
  private val messageSender: ISendMessage = PeripheralStateHandlerThread(peripheralControllerDelegate, peripheralListener)

  init {
      peripheralControllerDelegate.setHandlerThread(messageSender)
  }

  fun start(serviceUUID: UUID, scanRespUUID: UUID, advPayload: String,  scanRespPayload: String) {
    val advStartCmd = AdvertisementStartMessage(
      IMessage.PeripheralStates.ADV_START,
      serviceUUID,
      scanRespUUID,
      advPayload,
      scanRespPayload
    )
    messageSender.sendMessage(advStartCmd)
  }
}
