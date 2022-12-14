package io.mosip.tuvali.wallet

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanRecord
import android.content.Context
import android.os.HandlerThread
import android.os.ParcelUuid
import android.os.Process
import android.util.Log
import io.mosip.tuvali.ble.central.Central
import io.mosip.tuvali.ble.central.ICentralListener
import io.mosip.tuvali.cryptography.SecretsTranslator
import io.mosip.tuvali.cryptography.WalletCryptoBox
import io.mosip.tuvali.cryptography.WalletCryptoBoxBuilder
import com.facebook.react.bridge.Callback
import io.mosip.tuvali.openid4vpble.Openid4vpBleModule
import io.mosip.tuvali.transfer.TransferReport
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.verifier.GattService
import io.mosip.tuvali.verifier.Verifier
import io.mosip.tuvali.wallet.transfer.ITransferListener
import io.mosip.tuvali.wallet.transfer.TransferHandler
import io.mosip.tuvali.wallet.transfer.message.*
import org.bouncycastle.util.encoders.Hex
import java.security.SecureRandom
import java.util.*

class Wallet(context: Context, private val responseListener: (String, String) -> Unit) :
  ICentralListener, ITransferListener {
  private val logTag = "Wallet"

  private val secureRandom: SecureRandom = SecureRandom()
  private lateinit var verifierPK: ByteArray
  private var walletCryptoBox: WalletCryptoBox = WalletCryptoBoxBuilder.build(secureRandom)
  private var secretsTranslator: SecretsTranslator? = null

  private var advIdentifier: String? = null;
  private var transferHandler: TransferHandler
  private val handlerThread =
    HandlerThread("TransferHandlerThread", Process.THREAD_PRIORITY_DEFAULT)

  private var central: Central
  private val maxMTU = 517

  private enum class CentralCallbacks {
    CONNECTION_ESTABLISHED,
  }

  private val callbacks = mutableMapOf<CentralCallbacks, Callback>()

  init {
    central = Central(context, this@Wallet)
    handlerThread.start()
    transferHandler = TransferHandler(handlerThread.looper, central, Verifier.SERVICE_UUID, this@Wallet)
  }

  fun stop() {
    central.stop()
    handlerThread.quitSafely()
  }

  fun startScanning(advIdentifier: String, connectionEstablishedCallback: Callback) {
    callbacks[CentralCallbacks.CONNECTION_ESTABLISHED] = connectionEstablishedCallback
    central.scan(
      Verifier.SERVICE_UUID,
      advIdentifier
    )
  }

  fun writeIdentity() {
    val publicKey = walletCryptoBox.publicKey()
    secretsTranslator = walletCryptoBox.buildSecretsTranslator(verifierPK)
    val iv = secretsTranslator?.initializationVector()
    central.write(
      Verifier.SERVICE_UUID,
      GattService.IDENTITY_CHARACTERISTIC_UUID,
      iv!! + publicKey!!
    )
    Log.d(
      logTag,
      "Started to write - generated IV ${
        Hex.toHexString(iv)
      }, Public Key of wallet: ${Hex.toHexString(publicKey)}"
    )
  }

  override fun onScanStartedFailed(errorCode: Int) {
    Log.d(logTag, "onScanStartedFailed: $errorCode")
    //TODO: Handle error
  }

  override fun onDeviceFound(device: BluetoothDevice, scanRecord: ScanRecord?) {
    val scanResponsePayload =
      scanRecord?.getServiceData(ParcelUuid(Verifier.SCAN_RESPONSE_SERVICE_UUID))
    val advertisementPayload = scanRecord?.getServiceData(ParcelUuid(Verifier.SERVICE_UUID))

    //TODO: Handle multiple calls while connecting
    if (advertisementPayload != null && isSameAdvIdentifier(advertisementPayload) && scanResponsePayload != null) {
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

    Log.d(logTag, "Public Key of Verifier: ${Hex.toHexString(verifierPK)}")
  }

  private fun isSameAdvIdentifier(advertisementPayload: ByteArray): Boolean {
    this.advIdentifier?.let {
      return Hex.decode(it) contentEquals advertisementPayload
    }
    return false
  }

  override fun onDeviceConnected(device: BluetoothDevice) {
    Log.d(logTag, "onDeviceConnected")
    walletCryptoBox = WalletCryptoBoxBuilder.build(secureRandom)
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
    //TODO: Can we pass this MTU value to chunker, would this callback always come?
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
    Log.d(logTag, "Read from $charUUID successfully and value is $value")

    when (charUUID) {
      GattService.SEMAPHORE_CHAR_UUID -> {
      }
    }
  }

  override fun onReadFailure(charUUID: UUID?, err: Int) {
    when (charUUID) {
      GattService.SEMAPHORE_CHAR_UUID -> {
      }
    }
  }

  override fun onSubscriptionSuccess(charUUID: UUID) {
    // Do nothing
  }

  override fun onSubscriptionFailure(charUUID: UUID, err: Int) {
    //TODO: Close and send event to higher layer
  }

  override fun onDeviceDisconnected() {
    //TODO: Close and send event to higher layer
  }

  override fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, err: Int) {
    Log.d(logTag, "Failed to write char: $charUUID with error code: $err")

    when(charUUID) {
      GattService.RESPONSE_CHAR_UUID -> {
        transferHandler.sendMessage(ResponseChunkWriteFailureMessage(err))
      }
      GattService.SEMAPHORE_CHAR_UUID -> {
      transferHandler.sendMessage(ResponseTransferFailureMessage("Failed to request report with err: $err"))
      }
    }
  }

  // TODO: move all subscriptions and unsubscriptions to one place

  override fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID) {
    Log.d(logTag, "Wrote to $charUUID successfully")
    when (charUUID) {
      GattService.IDENTITY_CHARACTERISTIC_UUID -> {
        responseListener("exchange-receiver-info", "{\"deviceName\": \"Verifier\"}")
      }
      GattService.RESPONSE_SIZE_CHAR_UUID -> {
        transferHandler.sendMessage(ResponseSizeWriteSuccessMessage())
      }
      GattService.RESPONSE_CHAR_UUID -> {
        transferHandler.sendMessage(ResponseChunkWriteSuccessMessage())
      }
      GattService.SEMAPHORE_CHAR_UUID -> {
        central.subscribe(Verifier.SERVICE_UUID, GattService.SEMAPHORE_CHAR_UUID)
        central.subscribe(Verifier.SERVICE_UUID, GattService.VERIFICATION_STATUS_CHAR_UUID)
      }
    }
  }

  override fun onResponseSent() {
  }

  override fun onResponseSendFailure(errorMsg: String) {
    // TODO: Handle error
  }

  override fun onNotificationReceived(charUUID: UUID, value: ByteArray?) {
    when (charUUID) {
      GattService.SEMAPHORE_CHAR_UUID -> {
        value?.let {
          transferHandler.sendMessage(HandleTransmissionReportMessage(TransferReport(it)))
        }
      }
      GattService.VERIFICATION_STATUS_CHAR_UUID -> {
        val status = value?.get(0)?.toInt()
        if(status != null && status == TransferHandler.VerificationStates.ACCEPTED.ordinal) {
          responseListener("send-vc:response", Openid4vpBleModule.InjiVerificationStates.ACCEPTED.value)
        } else {
          responseListener("send-vc:response", Openid4vpBleModule.InjiVerificationStates.REJECTED.value)
        }

        central.unsubscribe(Verifier.SERVICE_UUID, charUUID)
        central.disconnect()
        central.close()
      }
    }
  }

  fun setAdvIdentifier(advIdentifier: String) {
    this.advIdentifier = advIdentifier
  }

  fun sendData(data: String) {
    val dataInBytes = data.toByteArray()
    Log.d(logTag, "dataInBytes size: ${dataInBytes.size}")
    val compressedBytes = Util.compress(dataInBytes)
    Log.e(logTag, "compression before: ${dataInBytes.size} and after: ${compressedBytes?.size}")
    val encryptedData = secretsTranslator?.encryptToSend(compressedBytes)
    if (encryptedData != null) {
      Log.d(logTag, "encryptedData size: ${encryptedData.size}, sha256: ${Util.getSha256(encryptedData)}")
      transferHandler.sendMessage(InitResponseTransferMessage(encryptedData))
    } else {
      Log.e(logTag, "failed to encrypt data with size: ${dataInBytes.size} and compressed size: ${compressedBytes?.size}")
    }
  }
}
