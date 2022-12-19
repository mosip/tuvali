package com.wallet

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.ble.central.Central
import com.ble.central.ICentralListener
import com.facebook.react.bridge.Callback
import com.verifier.GattService
import com.verifier.Verifier
import java.util.*

class Wallet(context: Context, private val responseListener: (String, String) -> Unit) : ICentralListener {
  private val logTag = "Wallet"
  private var publicKey: String = "b0f8980279d4df9f383bfd6e990b45c5fcba1c4fbef76c27b9141dff50b97984"
  private var IV: String = "DUMMY"
  private lateinit var walletPk: String;
  private var central: Central

  private enum class CentralCallbacks {
    CONNECTION_ESTABLISHED,
  }

  private val callbacks = mutableMapOf<CentralCallbacks, Callback>()

  init {
    central = Central(context, this@Wallet)
  }

  fun generateKeyPair(): String {
    return publicKey
  }

  fun startScanning(advIdentifier: String, connectionEstablishedCallback: Callback) {
    callbacks[CentralCallbacks.CONNECTION_ESTABLISHED] = connectionEstablishedCallback

    central.scan(
      Verifier.SERVICE_UUID,
      advIdentifier
    )
  }

  fun writeIdentity() {
    central.write(Verifier.SERVICE_UUID, GattService.IDENTITY_CHARACTERISTIC_UUID,"${IV}_$publicKey")
  }

  override fun onScanStartedFailed(errorCode: Int) {
    Log.d(logTag, "onScanStartedFailed: $errorCode")
  }

  override fun onDeviceFound(device: BluetoothDevice) {
    Log.d(logTag, "onDeviceFound")

    central.connect(device)
  }

  override fun onDeviceConnected(device: BluetoothDevice) {
    val connectionEstablishedCallBack = callbacks[CentralCallbacks.CONNECTION_ESTABLISHED]

    connectionEstablishedCallBack?.let {
      it()
      //TODO: Why this is getting called multiple times?. (Calling callback multiple times raises a exception)
      callbacks.remove(CentralCallbacks.CONNECTION_ESTABLISHED)
    }
  }

  override fun onDeviceDisconnected() {
      //TODO "Not yet implemented"
  }

  override fun onKeyExchanged() {
    Log.d(logTag, "onKeyExchanged")

    val connectionEstablishedCallback = callbacks[CentralCallbacks.CONNECTION_ESTABLISHED]
    connectionEstablishedCallback?.let { it() }
  }

  override fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, err: Int) {
    Log.d(logTag, "Failed to write char: $charUUID with error code: $err")
  }

  override fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID) {
    Log.d(logTag, "Wrote to $charUUID successfully")

    responseListener("exchange-receiver-info", "{\"deviceName\": \"Verifier dummy\"}")
  }

  fun setVerifierKey(walletPk: String) {
    this.walletPk = walletPk
  }

}
