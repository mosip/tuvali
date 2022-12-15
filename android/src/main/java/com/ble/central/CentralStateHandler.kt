package com.ble.central

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.ICentralListener
import com.ble.central.statemessage.ICentralMessage
import com.ble.central.statemessage.ScanStartFailureMessage
import com.ble.central.statemessage.ScanStartMessage

class CentralStateHandler(
  looper: Looper,
  private val centralControllerDelegate: CentralControllerDelegate,
  private val centralListener: ICentralListener
) : Handler(looper) {
  private val logTag = "CentralHandlerThread"
  override fun handleMessage(msg: Message) {
    when (msg.what) {
      ICentralMessage.CentralStates.SCAN_START.ordinal -> {
        Log.d(logTag, "start scan")
        centralControllerDelegate.scan(msg.obj as ScanStartMessage)
      }
      ICentralMessage.CentralStates.SCAN_START_SUCCESS.ordinal -> {
        Log.d(logTag, "scan started successfully")
        centralListener.onScanStartedSuccessfully()
      }
      ICentralMessage.CentralStates.SCAN_START_FAILURE.ordinal -> {
        Log.d(logTag, "scan start failed")
        val failureMsg = msg.obj as ScanStartFailureMessage
        centralListener.onScanStartedFailed(failureMsg.errorCode)
      }
    }
  }
}
