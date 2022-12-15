package com.ble.central

import android.os.Handler
import android.os.HandlerThread
import com.ble.ICentralListener
import com.ble.central.statemessage.ICentralMessage

class CentralStateHandlerThread (
  private val centralControllerDelegate: CentralControllerDelegate,
  private val centralListener: ICentralListener
) :
  HandlerThread("CentralHandlerThread", android.os.Process.THREAD_PRIORITY_DEFAULT),
  ICentralSendMessage {
  lateinit var handler: Handler;

  override fun onLooperPrepared() {
    handler = CentralStateHandler(this.looper, centralControllerDelegate, centralListener)
  }

  override fun sendMessage(msg: ICentralMessage) {
    val message = handler.obtainMessage()
    message.what = msg.commandType.ordinal
    message.obj = msg
    handler.sendMessage(message)
  }
}
