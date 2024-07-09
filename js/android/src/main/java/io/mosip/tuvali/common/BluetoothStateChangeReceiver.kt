package io.mosip.tuvali.common

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.mosip.tuvali.transfer.Util
import kotlin.reflect.KFunction2


open class BluetoothStateChangeReceiver(
  private val onDeviceNotConnected: (Int, Int) -> Unit,
) : BroadcastReceiver() {

  private val logTag = Util.getLogTag(javaClass.simpleName)
  override fun onReceive(context: Context?, intent: Intent?) {
    val action = intent?.action
    if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
      val device: BluetoothDevice? = intent?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
      val state: Int? = intent?.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
      val previousState =
        intent!!.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.ERROR)

      when (state) {
        BluetoothAdapter.STATE_OFF -> {
          Log.i(logTag,"STATE_OFF Bluetooth state change has happened device address: ${device?.address}")
          this.onDeviceNotConnected(previousState,state)
        }
        else -> {
          Log.d(logTag,"Bluetooth state change has happened - $state")
        }
      }
    }
  }
}
