package com.wallet

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.os.HandlerThread
import android.os.ParcelUuid
import android.os.Process
import android.util.Log
import com.ble.central.Central
import com.ble.central.ICentralListener
import com.cryptography.SecretsTranslator
import com.cryptography.WalletCryptoBoxBuilder
import com.facebook.common.util.Hex
import com.facebook.react.bridge.Callback
import com.verifier.GattService
import com.verifier.Verifier
import com.wallet.transfer.TransferHandler
import com.wallet.transfer.message.ChunkWriteToRemoteStatusUpdatedMessage
import com.wallet.transfer.message.InitResponseTransferMessage
import com.wallet.transfer.message.ResponseSizeWriteSuccessMessage
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
  private var transferHandler: TransferHandler
  private val handlerThread = HandlerThread("TransferHandlerThread", Process.THREAD_PRIORITY_DEFAULT)
  private var central: Central
  private val maxMTU = 517

  private enum class CentralCallbacks {
    CONNECTION_ESTABLISHED,
  }

  private val callbacks = mutableMapOf<CentralCallbacks, Callback>()

  init {
    central = Central(context, this@Wallet)
    handlerThread.start()
    transferHandler = TransferHandler(handlerThread.looper, central, Verifier.SERVICE_UUID)
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
    Log.d(logTag, "Started to write - generated IV ${Hex.encodeHex(iv, false)}, Public Key of wallet: ${Hex.encodeHex(publicKey, false)}")
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
    this.advIdentifier?.let {
      return Hex.decodeHex(it) contentEquals advertisementPayload
    }
    return false
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

  override fun onReadSuccess(charUUID: UUID, value: ByteArray?) {
    Log.d(logTag, "Read to $charUUID successfully")

    when(charUUID) {
      GattService.SEMAPHORE_CHAR_UUID -> {
        if (value != null && value.isNotEmpty()) {
          transferHandler.sendMessage(ChunkWriteToRemoteStatusUpdatedMessage(value[0].toInt()))
        }
      }
    }
  }

  override fun onReadFailure(charUUID: UUID?, err: Int) {
    // TODO("Not yet implemented")
  }

  override fun onDeviceDisconnected() {
    //TODO Handle Disconnect
  }

  override fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, err: Int) {
    Log.d(logTag, "Failed to write char: $charUUID with error code: $err")
  }

  override fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID) {
    Log.d(logTag, "Wrote to $charUUID successfully")
    when (charUUID) {
      GattService.IDENTITY_CHARACTERISTIC_UUID -> {
        responseListener("exchange-receiver-info", "{\"deviceName\": \"Verifier\"}")
      }
      GattService.RESPONSE_SIZE_CHAR_UUID -> {
        transferHandler.sendMessage(ResponseSizeWriteSuccessMessage())
      }
    }
  }

  fun setAdvIdentifier(advIdentifier: String) {
    this.advIdentifier = advIdentifier
  }

  fun sendData(vcData: String) {
    transferHandler.sendMessage(InitResponseTransferMessage(vcData.toByteArray()))
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
