package com.ble

import android.content.Context
import com.ble.central.CentralControllerDelegate
import com.ble.central.CentralStateHandlerThread
import com.ble.central.ICentralSendMessage
import com.ble.central.statemessage.ScanStartMessage
import java.util.*

class Central(context: Context, centralLister: ICentralListener) {
  private val centralControllerDelegate: CentralControllerDelegate = CentralControllerDelegate(context)
  private val messageSender: ICentralSendMessage = CentralStateHandlerThread(centralControllerDelegate, centralLister)

  init {
    centralControllerDelegate.setHandlerThread(messageSender)
  }

  fun scan(serviceUuid: UUID, scanResponseServiceUuid: UUID, advIdentifier: String) {
    val scanStartMessage = ScanStartMessage(serviceUuid, scanResponseServiceUuid, advIdentifier)

    messageSender.sendMessage(scanStartMessage)
  }

}
