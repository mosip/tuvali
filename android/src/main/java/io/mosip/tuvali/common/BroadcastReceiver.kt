package io.mosip.tuvali.common

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.util.Log
import io.mosip.tuvali.common.events.EventEmitter
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.wallet.IWallet

open class BroadcastReceiver(
  private val wallet: IWallet,
) : android.content.BroadcastReceiver() {
  private val logTag = Util.getLogTag(javaClass.simpleName)
  private var eventEmitter = EventEmitter()
  override fun onReceive(p0: Context?, intent: Intent?) {
    Log.d(logTag, "BroadcastReceiver overridden onReceive $p0 ${intent?.extras}")
    val action = intent?.action
    if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

      val state: Int? = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
      when (state) {
        BluetoothAdapter.STATE_OFF -> {
          Log.d(logTag, "STATE_OFF Bluetooth state change has happened $state")
          wallet.handleDisconnect()

        }
        else -> {
          Log.d(logTag,"Bluetooth state change has happened - $state")
        }
      }
    }
  }
}
