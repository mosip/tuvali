package com.verifier

import android.content.Context
import android.util.Log
import com.ble.peripheral.IPeripheralListener
import com.ble.peripheral.Peripheral
import com.facebook.react.bridge.Callback
import java.util.*

class Verifier(context: Context, private val responseListener: (String) -> Unit):
  IPeripheralListener {
  private val logTag = "Verifier"
  private var publicKey: String = "b0f8980279d4df9f383bfd6e990b45c5fcba1c4fbef76c27b9141dff50b97983"
  private var peripheral: Peripheral

  //TODO: Update UUIDs as per specification
  companion object {
    val SERVICE_UUID: UUID = UUID.fromString("0000AB29-0000-1000-8000-00805f9b34fb")
    val SCAN_RESPONSE_SERVICE_UUID: UUID = UUID.fromString("0000AB2A-0000-1000-8000-00805f9b34fb")
  }

  private enum class PeripheralCallbacks {
    ADV_SUCCESS_CALLBACK,
    ADV_FAILURE_CALLBACK
  }
  private val callbacks = mutableMapOf<PeripheralCallbacks, Callback>()

  init {
    peripheral = Peripheral(context, this@Verifier)
  }

  fun generateKeyPair(): String {
    return publicKey
  }

  // TODO: Need identifier from higher layer to form adv payload
  fun startAdvertisement(advIdentifier: String, successCallback: Callback) {
    callbacks[PeripheralCallbacks.ADV_SUCCESS_CALLBACK] = successCallback
    peripheral.start(
      SERVICE_UUID,
      SCAN_RESPONSE_SERVICE_UUID,
      getAdvPayload(advIdentifier),
      getScanRespPayload()
    )
  }

  override fun onAdvertisementStartSuccessful() {
    Log.d(logTag, "onAdvertisementStartSuccess")
    val successCallback = callbacks[PeripheralCallbacks.ADV_SUCCESS_CALLBACK]
    successCallback?.let { it() }
  }

  override fun onAdvertisementStartFailed(errorCode: Int) {
    Log.d(logTag, "onAdvertisementStartFailed: $errorCode")
  }

  private fun getAdvPayload(advIdentifier: String): String {
    return advIdentifier + "_" + publicKey.substring(0,5)
  }

  private fun getScanRespPayload(): String {
    return publicKey.substring(5, 32)
  }
}
