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
import io.mosip.tuvali.common.retrymechanism.BackOffStrategy
import io.mosip.tuvali.transfer.TransferReport
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.verifier.GattService
import io.mosip.tuvali.verifier.Verifier
import io.mosip.tuvali.verifier.Verifier.Companion.DISCONNECT_STATUS
import io.mosip.tuvali.wallet.exception.MTUNegotiationFailedException
import io.mosip.tuvali.wallet.transfer.ITransferListener
import io.mosip.tuvali.wallet.transfer.TransferHandler
import io.mosip.tuvali.wallet.transfer.message.*
import org.bouncycastle.util.encoders.Hex
import java.security.SecureRandom
import java.util.*
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

private const val MTU_REQUEST_RETRY_DELAY_TIME_IN_MILLIS = 500L

class Wallet(
  context: Context,
  private val messageResponseListener: (String, String) -> Unit,
  private val eventResponseListener: (String) -> Unit,
  private val onBLEException: (Throwable) -> Unit
) : ICentralListener, ITransferListener {
  private val logTag = getLogTag(javaClass.simpleName)

  private val secureRandom: SecureRandom = SecureRandom()
  private lateinit var verifierPK: ByteArray
  private var walletCryptoBox: WalletCryptoBox = WalletCryptoBoxBuilder.build(secureRandom)
  private var secretsTranslator: SecretsTranslator? = null

  private var advIdentifier: String? = null
  private var transferHandler: TransferHandler
  private val handlerThread = HandlerThread("TransferHandlerThread", Process.THREAD_PRIORITY_DEFAULT)

  private var central: Central

  //default mtu is 23 bytes and the allowed data bytes is 20 bytes
  private var maxDataBytes = 20
  private val mtuValues = arrayOf(512, 185, 100)

  private val retryDiscoverServices = BackOffStrategy(maxRetryLimit = 5)

  private enum class CentralCallbacks {
    CONNECTION_ESTABLISHED,
    ON_DESTROY_SUCCESS_CALLBACK
  }

  private val callbacks = mutableMapOf<CentralCallbacks, Callback>()

  init {
    central = Central(context, this@Wallet)
    handlerThread.start()
    transferHandler = TransferHandler(handlerThread.looper, central, Verifier.SERVICE_UUID, this@Wallet)
  }

  fun stop(onDestroy: Callback) {
    callbacks[CentralCallbacks.ON_DESTROY_SUCCESS_CALLBACK] = onDestroy
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

  fun writeToIdentifyRequest() {
    val publicKey = walletCryptoBox.publicKey()
    secretsTranslator = walletCryptoBox.buildSecretsTranslator(verifierPK)
    val iv = secretsTranslator?.initializationVector()
    central.write(
      Verifier.SERVICE_UUID,
      GattService.IDENTIFY_REQUEST_CHAR_UUID,
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

  private val connectionMutex = Object()
  @Volatile private var connectionState = VerifierConnectionState.NOT_CONNECTED

  private enum class VerifierConnectionState {
    NOT_CONNECTED,
    CONNECTING,
    CONNECTED
  }

  override fun onDeviceFound(device: BluetoothDevice, scanRecord: ScanRecord?) {
    synchronized(connectionMutex) {
      if (connectionState != VerifierConnectionState.NOT_CONNECTED) {
        Log.d(
          logTag, "Device found is ignored $device"
        )
        return
      }
      val scanResponsePayload =
        scanRecord?.getServiceData(ParcelUuid(Verifier.SCAN_RESPONSE_SERVICE_UUID))
      val advertisementPayload = scanRecord?.getServiceData(ParcelUuid(Verifier.SERVICE_UUID))

      if (advertisementPayload != null && isSameAdvIdentifier(advertisementPayload) && scanResponsePayload != null) {
        setVerifierPK(advertisementPayload, scanResponsePayload)
        central.connect(device)
        connectionState = VerifierConnectionState.CONNECTING
      } else {
        Log.d(
          logTag, "AdvIdentifier($advIdentifier) is not matching with peripheral device adv"
        )
      }
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
    synchronized(connectionMutex) {
      Log.d(logTag, "on Verifier device connected")
      connectionState = VerifierConnectionState.CONNECTED

      Log.d(logTag, "stopping scan for verifiers")
      central.stopScan()

      walletCryptoBox = WalletCryptoBoxBuilder.build(secureRandom)
      central.discoverServices()
    }
  }

  override fun onServicesDiscovered(serviceUuids: List<UUID>) {

    if (serviceUuids.contains(Verifier.SERVICE_UUID)) {
      retryDiscoverServices.reset()
      Log.d(logTag, "onServicesDiscovered with services - $serviceUuids")
      central.requestMTU(mtuValues, MTU_REQUEST_RETRY_DELAY_TIME_IN_MILLIS)
    } else {
      retryServiceDiscovery()
    }
  }
  override fun onServicesDiscoveryFailed(errorCode: Int) {
    Log.d(logTag, "onServicesDiscoveryFailed retrying to find the services")
    retryServiceDiscovery()
  }

  private fun retryServiceDiscovery() {
    if (retryDiscoverServices.shouldRetry()) {
      central.discoverServicesDelayed(retryDiscoverServices.getWaitTime())
    } else {
      //TODO: Send service discovery failure to inji layer
      Log.d(logTag, "Retrying to find the services failed after multiple attempts")
      retryDiscoverServices.reset()
    }
  }

  override fun onRequestMTUSuccess(mtu: Int) {
    Log.d(logTag, "onRequestMTUSuccess")
    maxDataBytes = mtu
    val connectionEstablishedCallBack = callbacks[CentralCallbacks.CONNECTION_ESTABLISHED]
    central.subscribe(Verifier.SERVICE_UUID, GattService.DISCONNECT_CHAR_UUID)

    connectionEstablishedCallBack?.let {
      it()
      //TODO: Why this is getting called multiple times?. (Calling callback multiple times raises a exception)
      callbacks.remove(CentralCallbacks.CONNECTION_ESTABLISHED)
    }
  }

  override fun onRequestMTUFailure(errorCode: Int) {
    //TODO: Handle onRequest MTU failure
    throw  MTUNegotiationFailedException("MTU negotiation failed even after multiple retries  with error code: $errorCode.")
  }

  override fun onReadSuccess(charUUID: UUID, value: ByteArray?) {
    Log.d(logTag, "Read from $charUUID successfully and value is $value")
  }

  override fun onReadFailure(charUUID: UUID?, err: Int) {}

  override fun onSubscriptionSuccess(charUUID: UUID) {
    // Do nothing
  }

  override fun onSubscriptionFailure(charUUID: UUID, err: Int) {
    //TODO: Close and send event to higher layer
  }

  override fun onDeviceDisconnected(isManualDisconnect: Boolean) {
    synchronized(connectionMutex) {
      connectionState = VerifierConnectionState.NOT_CONNECTED
      if (!isManualDisconnect) {
        eventResponseListener("onDisconnected")
      }
    }
  }

  override fun onWriteFailed(device: BluetoothDevice, charUUID: UUID, err: Int) {
    Log.d(logTag, "Failed to write char: $charUUID with error code: $err")

    when(charUUID) {
      GattService.SUBMIT_RESPONSE_CHAR_UUID -> {
        transferHandler.sendMessage(ResponseChunkWriteFailureMessage(err))
      }
      GattService.TRANSFER_REPORT_REQUEST_CHAR_UUID -> {
      transferHandler.sendMessage(ResponseTransferFailureMessage("Failed to request report with err: $err"))
      }
    }
  }

  // TODO: move all subscriptions and unsubscriptions to one place

  override fun onWriteSuccess(device: BluetoothDevice, charUUID: UUID) {
    Log.d(logTag, "Wrote to $charUUID successfully")
    when (charUUID) {
      GattService.IDENTIFY_REQUEST_CHAR_UUID -> {
        messageResponseListener(Openid4vpBleModule.NearbyEvents.EXCHANGE_RECEIVER_INFO.value, "{\"deviceName\": \"Verifier\"}")
      }
      GattService.RESPONSE_SIZE_CHAR_UUID -> {
        transferHandler.sendMessage(ResponseSizeWriteSuccessMessage())
      }
      GattService.SUBMIT_RESPONSE_CHAR_UUID -> {
        transferHandler.sendMessage(ResponseChunkWriteSuccessMessage())
      }
      GattService.TRANSFER_REPORT_REQUEST_CHAR_UUID -> {
        central.subscribe(Verifier.SERVICE_UUID, GattService.TRANSFER_REPORT_RESPONSE_CHAR_UUID)
        central.subscribe(Verifier.SERVICE_UUID, GattService.VERIFICATION_STATUS_CHAR_UUID)
      }
    }
  }

  override fun onResponseSent() {
    messageResponseListener(Openid4vpBleModule.NearbyEvents.SEND_VC_RESPONSE.value, Openid4vpBleModule.VCResponseStates.RECEIVED.value)
  }

  override fun onResponseSendFailure(errorMsg: String) {
    // TODO: Handle error
  }

  override fun onNotificationReceived(charUUID: UUID, value: ByteArray?) {
    when (charUUID) {
      GattService.TRANSFER_REPORT_RESPONSE_CHAR_UUID -> {
        value?.let {
          transferHandler.sendMessage(HandleTransmissionReportMessage(TransferReport(it)))
        }
      }
      GattService.VERIFICATION_STATUS_CHAR_UUID -> {
        val status = value?.get(0)?.toInt()
        if(status != null && status == TransferHandler.VerificationStates.ACCEPTED.ordinal) {
          messageResponseListener(Openid4vpBleModule.NearbyEvents.SEND_VC_RESPONSE.value, Openid4vpBleModule.VCResponseStates.ACCEPTED.value)
        } else {
          messageResponseListener(Openid4vpBleModule.NearbyEvents.SEND_VC_RESPONSE.value, Openid4vpBleModule.VCResponseStates.REJECTED.value)
        }

        central.unsubscribe(Verifier.SERVICE_UUID, charUUID)
        central.disconnectAndClose()
      }
      GattService.DISCONNECT_CHAR_UUID -> {
        val status = value?.get(0)?.toInt()

        if(status != null && status == DISCONNECT_STATUS) {
          central.unsubscribe(Verifier.SERVICE_UUID, charUUID)
          central.disconnectAndClose()
        }
      }
    }
  }

  override fun onException(exception: Throwable) {
    onBLEException(exception)
  }

  override fun onClosed() {
    Log.d(logTag, "onClosed")
    central.quitHandler()
    val onClosedCallback = callbacks[CentralCallbacks.ON_DESTROY_SUCCESS_CALLBACK]

    onClosedCallback?.let {
      Log.d(logTag, "calling onDestroy callback")
      it()
      callbacks.remove(CentralCallbacks.ON_DESTROY_SUCCESS_CALLBACK)
    }
  }

  fun setAdvIdentifier(advIdentifier: String) {
    this.advIdentifier = advIdentifier
  }

  fun sendData(data: String) {
    val dataInBytes = data.toByteArray()
    Log.d(logTag, "dataInBytes size: ${dataInBytes.size}")
    val compressedBytes = Util.compress(dataInBytes)
    Log.d(logTag, "compression before: ${dataInBytes.size} and after: ${compressedBytes?.size}")
    try {
      val encryptedData = secretsTranslator?.encryptToSend(compressedBytes)
      if (encryptedData != null) {
        //Log.d(logTag, "Complete Encrypted Data: ${Hex.toHexString(encryptedData)}")
        Log.i(logTag, "Sha256 of Encrypted Data: ${Util.getSha256(encryptedData)}")
        transferHandler.sendMessage(InitResponseTransferMessage(encryptedData, maxDataBytes))
      } else {
        Log.e(
          logTag, "encrypted data is null, with size: ${dataInBytes.size} and compressed size: ${compressedBytes?.size}"
        )
      }
    } catch (e: Exception) {
        Log.e(logTag, "failed to encrypt with size: ${dataInBytes.size} and compressed size ${compressedBytes?.size}, with exception: ${e.message}, stacktrace: ${e.stackTraceToString()}")
    }
  }
}
