package com.wallet

import android.content.Context
import android.util.Log
import com.ble.Central
import com.ble.ICentralListener
import com.facebook.react.bridge.Callback
import com.verifier.Verifier

class Wallet(context: Context, private val responseListener: (String) -> Unit): ICentralListener {
  private val logTag = "Wallet"
  private var publicKey: String = "b0f8980279d4df9f383bfd6e990b45c5fcba1c4fbef76c27b9141dff50b97984"
  private var central: Central

  private enum class CentralCallbacks {
    ADV_SUCCESS_CALLBACK,
    ADV_FAILURE_CALLBACK
  }
  private val callbacks = mutableMapOf<CentralCallbacks, Callback>()

  init {
    central = Central(context, this@Wallet)
  }

  fun generateKeyPair(): String {
    return publicKey
  }

  fun startScanning(advIdentifier: String, successCallback: Callback) {
    callbacks[CentralCallbacks.ADV_SUCCESS_CALLBACK] = successCallback
    central.scan(
      Verifier.SERVICE_UUID,
      Verifier.SCAN_RESPONSE_SERVICE_UUID,
      advIdentifier
    )
  }

  override fun onScanStartedSuccessfully() {
    val successCallback = callbacks[CentralCallbacks.ADV_SUCCESS_CALLBACK]
    successCallback?.let { it() }
  }

  override fun onScanStartedFailed(errorCode: Int) {
    Log.d(logTag, "onScanStartedFailed: $errorCode")
  }

}
