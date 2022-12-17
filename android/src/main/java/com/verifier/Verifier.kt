package com.verifier

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import com.ble.peripheral.IPeripheralListener
import com.ble.peripheral.Peripheral
import com.facebook.react.bridge.Callback
import java.util.*
import kotlin.reflect.KFunction1

class Verifier(context: Context, private val responseListener: (String, String) -> Unit) :
  IPeripheralListener {
  private val logTag = "Verifier"
  private var publicKey: String = "b0f8980279d4df9f383bfd6e990b45c5fcba1c4fbef76c27b9141dff50b97983"
  private lateinit var walletPubKey: String
  private lateinit var iv: String
  private var peripheral: Peripheral

  //TODO: Update UUIDs as per specification
  companion object {
    val SERVICE_UUID: UUID = UUID.fromString("0000AB29-0000-1000-8000-00805f9b34fb")
    val SCAN_RESPONSE_SERVICE_UUID: UUID = UUID.fromString("0000AB2A-0000-1000-8000-00805f9b34fb")
    val IDENTITY_CHARACTERISTIC_UUID: UUID = UUID.fromString("00002030-0000-1000-8000-00805f9b34fb")
  }

  private enum class PeripheralCallbacks {
    ADV_SUCCESS_CALLBACK,
    ADV_FAILURE_CALLBACK
  }

  private val callbacks = mutableMapOf<PeripheralCallbacks, Callback>()

  init {
    peripheral = Peripheral(context, this@Verifier)
    peripheral.setupService(createPeripheralService())
  }

  private fun createPeripheralService(): BluetoothGattService {
    val service = BluetoothGattService(
      SERVICE_UUID,
      BluetoothGattService.SERVICE_TYPE_PRIMARY
    )

    val identityChar = BluetoothGattCharacteristic(
      IDENTITY_CHARACTERISTIC_UUID,
      BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE or BluetoothGattCharacteristic.PROPERTY_WRITE,
      BluetoothGattCharacteristic.PERMISSION_WRITE
    )

    service.addCharacteristic(identityChar)
    return service
  }

  fun generateKeyPair(): String {
    return publicKey
  }

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

  override fun onReceivedWrite(uuid: UUID, value: ByteArray?) {
    if (uuid == IDENTITY_CHARACTERISTIC_UUID) {
      val identityValue = value.toString()
      var identitySubstrings = listOf<String>()
      if (identityValue !== "") {
        identitySubstrings = identityValue.split("_", limit =  2)
      }
      if (identitySubstrings.size > 1) {
        iv = identitySubstrings[0]
        walletPubKey = identitySubstrings[1]
      }
      // TODO: Validate pub key, how to handle if not valid?
      if (walletPubKey != "") {
        responseListener("exchange-sender-info", "{\"deviceName\": \"Wallet\"}")
      }
    }
  }

  // TODO: Can remove this
  override fun onDeviceConnected() {
    Log.d(logTag, "onDeviceConnected: sending event")
    responseListener("exchange-sender-info", "{\"deviceName\": \"Wallet\"}")
  }

  private fun getAdvPayload(advIdentifier: String): String {
    return advIdentifier + "_" + publicKey.substring(0, 5)
  }

  private fun getScanRespPayload(): String {
    return publicKey.substring(5, 32)
  }
}
