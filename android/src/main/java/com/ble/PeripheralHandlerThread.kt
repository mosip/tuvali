package com.ble

import android.os.Handler
import android.os.HandlerThread
import com.ble.handlercommand.ICommand

class PeripheralHandlerThread(private val peripheralHandlerDelegate: PeripheralHandlerDelegate) :
  HandlerThread("PeripheralHandlerThread", android.os.Process.THREAD_PRIORITY_DEFAULT) {
  lateinit var handler: Handler;

  override fun onLooperPrepared() {
    handler = PeripheralStateHandler(this.looper, peripheralHandlerDelegate)
  }

  public fun sendMessage(command: ICommand) {
    val message = handler.obtainMessage()
    message.what = command.commandType.ordinal
    message.obj = command
    handler.sendMessage(message)
  }
}
