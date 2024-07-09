package io.mosip.tuvali.rnModule

import android.bluetooth.BluetoothAdapter
import android.content.IntentFilter
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import io.mosip.tuvali.common.BluetoothStateChangeReceiver
import io.mosip.tuvali.wallet.IWallet

class RNWalletModule(
  private val eventEmitter: RNEventEmitter,
  private val wallet: IWallet,
  reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {
  private var bluetoothStateChangeIntentFilter: IntentFilter = IntentFilter()
  private val bluetoothStateChangeReceiver = BluetoothStateChangeReceiver(wallet::handleDisconnect)

  init {
    wallet.subscribe {
      eventEmitter.emitEvent(RNEventMapper.toMap(it))
    }
    bluetoothStateChangeIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    reactContext.registerReceiver(bluetoothStateChangeReceiver, bluetoothStateChangeIntentFilter)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun startConnection(uri: String) {
    wallet.startConnection(uri)
  }

  @ReactMethod
  fun sendData(payload: String) {
    wallet.sendData(payload)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun disconnect() {
    wallet.disconnect()
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "WalletModule"
  }

  protected fun finalize() {
    reactApplicationContext.unregisterReceiver(bluetoothStateChangeReceiver)
  }
}
