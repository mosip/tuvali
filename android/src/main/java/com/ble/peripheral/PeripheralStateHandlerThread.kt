package com.ble.peripheral

import android.os.Handler
import android.os.HandlerThread
import com.ble.IPeripheralListener
import com.ble.statemessage.IMessage

class PeripheralStateHandlerThread(
  private val peripheralControllerDelegate: PeripheralControllerDelegate,
  private val peripheralListener: IPeripheralListener
) :
  HandlerThread("PeripheralHandlerThread", android.os.Process.THREAD_PRIORITY_DEFAULT),
  ISendMessage {
  lateinit var handler: Handler;

  override fun onLooperPrepared() {
    handler = PeripheralStateHandler(this.looper, peripheralControllerDelegate, peripheralListener)
  }

  override fun sendMessage(msg: IMessage) {
    val message = handler.obtainMessage()
    message.what = msg.commandType.ordinal
    message.obj = msg
    handler.sendMessage(message)
  }
}
