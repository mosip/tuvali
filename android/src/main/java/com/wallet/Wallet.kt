package com.wallet

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import com.ble.central.Central
import com.ble.central.ICentralListener
import com.cryptography.SecretsTranslator
import com.cryptography.WalletCryptoBoxBuilder
import com.facebook.common.util.Hex
import com.facebook.react.bridge.Callback
import com.verifier.GattService
import com.verifier.Verifier
import java.security.SecureRandom
import java.util.*

class Wallet(context: Context, private val responseListener: (String, String) -> Unit) : ICentralListener {
  private lateinit var verifierPK: ByteArray
  private lateinit var buildSecretsTranslator: SecretsTranslator
  private val logTag = "Wallet"
  private lateinit var iv: ByteArray;
  private val walletCipherBox = WalletCryptoBoxBuilder.build(SecureRandom())
  private val publicKey = walletCipherBox.publicKey()
  private var advIdentifier: String? = null;
  private var central: Central
  private val maxMTU = 517

  private enum class CentralCallbacks {
    CONNECTION_ESTABLISHED,
  }

  private val callbacks = mutableMapOf<CentralCallbacks, Callback>()

  init {
    central = Central(context, this@Wallet)
  }

  fun startScanning(advIdentifier: String, connectionEstablishedCallback: Callback) {
    callbacks[CentralCallbacks.CONNECTION_ESTABLISHED] = connectionEstablishedCallback
    central.scan(
      Verifier.SERVICE_UUID,
      advIdentifier
    )
  }

  fun writeIdentity() {
    central.write(Verifier.SERVICE_UUID, GattService.IDENTITY_CHARACTERISTIC_UUID,iv+publicKey)
    Log.d(logTag, "Public Key of wallet: ${Hex.encodeHex(publicKey, false)}")
  }

  override fun onScanStartedFailed(errorCode: Int) {
    Log.d(logTag, "onScanStartedFailed: $errorCode")
  }

  override fun onDeviceFound(device: BluetoothDevice, scanRecord: ScanRecord?) {
    val scanResponsePayload = scanRecord?.getServiceData(ParcelUuid(Verifier.SCAN_RESPONSE_SERVICE_UUID))
    val advertisementPayload = scanRecord?.getServiceData(ParcelUuid(Verifier.SERVICE_UUID))

    //TODO: Handle multiple calls while connecting
    if(advertisementPayload != null && isSameAdvIdentifier(advertisementPayload) && scanResponsePayload != null) {
      Log.d(logTag, "Stopping the scan.")
      central.stopScan()

      setVerifierPK(advertisementPayload, scanResponsePayload)
      central.connect(device)
    } else {
      Log.d(
        logTag,
        "AdvIdentifier($advIdentifier) is not matching with peripheral device adv"
      )
    }
  }

  private fun setVerifierPK(advertisementPayload: ByteArray, scanResponsePayload: ByteArray) {
    val first5BytesOfPkFromBLE = advertisementPayload.takeLast(5).toByteArray()
    this.verifierPK = first5BytesOfPkFromBLE + scanResponsePayload

    Log.d(logTag, "Public Key of Verifier: ${Hex.encodeHex(verifierPK, false)}")
  }

  private fun isSameAdvIdentifier(advertisementPayload: ByteArray): Boolean {
    return Hex.decodeHex(this.advIdentifier) contentEquals advertisementPayload
  }

  override fun onDeviceConnected(device: BluetoothDevice) {
    Log.d(logTag, "onDeviceConnected")

    buildSecretsTranslator = walletCipherBox.buildSecretsTranslator(verifierPK)
    iv = buildSecretsTranslator.initializationVector()

    central.discoverServices()
  }

  override fun onServicesDiscovered() {
    Log.d(logTag, "onServicesDiscovered")

    central.requestMTU(maxMTU)
  }

  override fun onServicesDiscoveryFailed(errorCode: Int) {
    Log.d(logTag, "onServicesDiscoveryFailed")
    //TODO: Handle services discovery failure
  }

  override fun onRequestMTUSuccess(mtu: Int) {
    Log.d(logTag, "onRequestMTUSuccess")
    val connectionEstablishedCallBack = callbacks[CentralCallbacks.CONNECTION_ESTABLISHED]

    connectionEstablishedCallBack?.let {
      it()
      //TODO: Why this is getting called multiple times?. (Calling callback multiple times raises a exception)
      callbacks.remove(CentralCallbacks.CONNECTION_ESTABLISHED)
    }
  }

  override fun onRequestMTUFailure(errorCode: Int) {
    //TODO: Handle onRequest MTU failure
  }

  override fun onDeviceDisconnected() {
    //TODO Handle Disconnect
  }

  override fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, err: Int) {
    Log.d(logTag, "Failed to write char: $charUUID with error code: $err")
  }

  override fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID) {
    Log.d(logTag, "Wrote to $charUUID successfully")

    responseListener("exchange-receiver-info", "{\"deviceName\": \"Verifier dummy\"}")
  }

  fun setAdvIdentifier(advIdentifier: String) {
    this.advIdentifier = advIdentifier
  }

}

//
//V ->
//Verifier: A695D3 055C4D5C5C3C396635AB18A3543AE893C86E6636B69B7E0D2726E36224
//wallet pub key: 11D61C37EC58B3ED2C0C0711A39A208FB9B09940574CA944FAD445A6F2D78710
//
//  W ->
//
//Verifier -> EFBFBDEFBFBDEFBFBD 055C4D5C5C3C396635AB18A3543AE893C86E6636B69B7E0D2726E36224
//W -> 11D61C37EC58B3ED2C0C0711A39A208FB9B09940574CA944FAD445A6F2D78710
//
