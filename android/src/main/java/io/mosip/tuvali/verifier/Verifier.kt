package io.mosip.tuvali.verifier

import android.content.Context
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_DEFAULT
import android.util.Log
import io.mosip.tuvali.ble.peripheral.IPeripheralListener
import io.mosip.tuvali.ble.peripheral.Peripheral
import io.mosip.tuvali.cryptography.SecretsTranslator
import io.mosip.tuvali.cryptography.VerifierCryptoBox
import io.mosip.tuvali.cryptography.VerifierCryptoBoxBuilder
import com.facebook.react.bridge.Callback
import io.mosip.tuvali.transfer.Semaphore
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.verifier.transfer.ITransferListener
import io.mosip.tuvali.verifier.transfer.TransferHandler
import io.mosip.tuvali.verifier.transfer.message.*
import org.bouncycastle.util.encoders.Hex
import java.security.SecureRandom
import java.util.*


class Verifier(context: Context, private val responseListener: (String, String) -> Unit) :
  IPeripheralListener, ITransferListener {
  private var secretsTranslator: SecretsTranslator? = null;
  private val logTag = "Verifier"
  private var publicKey: ByteArray = byteArrayOf()
  private lateinit var walletPubKey: ByteArray
  private lateinit var iv: ByteArray
  private var secureRandom: SecureRandom = SecureRandom()
  private var verifierCryptoBox: VerifierCryptoBox = VerifierCryptoBoxBuilder.build(secureRandom)
  private var peripheral: Peripheral
  private var transferHandler: TransferHandler
  private val handlerThread = HandlerThread("TransferHandlerThread", THREAD_PRIORITY_DEFAULT)

  //TODO: Update UUIDs as per specification
  companion object {
    val SERVICE_UUID: UUID = UUID.fromString("0000AB29-0000-1000-8000-00805f9b34fb")
    val SCAN_RESPONSE_SERVICE_UUID: UUID = UUID.fromString("0000AB2A-0000-1000-8000-00805f9b34fb")
  }

  private enum class PeripheralCallbacks {
    ADV_SUCCESS_CALLBACK,
    ADV_FAILURE_CALLBACK,
    DEVICE_CONNECTED_CALLBACK,
    RESPONSE_RECEIVE_SUCCESS_CALLBACK,
    RESPONSE_RECEIVED_FAILED_CALLBACK
  }

  private val callbacks = mutableMapOf<PeripheralCallbacks, Callback>()

  init {
    handlerThread.start()
    peripheral = Peripheral(context, this@Verifier)
    val gattService = GattService()
    peripheral.setupService(gattService.create())
    transferHandler = TransferHandler(handlerThread.looper, peripheral, this@Verifier, SERVICE_UUID)
  }

  fun stop() {
    peripheral.stop();
    handlerThread.quitSafely()
  }

  fun generateKeyPair() {
    verifierCryptoBox = VerifierCryptoBoxBuilder.build(secureRandom)
    publicKey = verifierCryptoBox.publicKey()
    Log.i(logTag, "Verifier public key: ${Hex.toHexString(publicKey)}")
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

  fun sendRequest(request: String, responseReceivedCallback: Callback) {
    callbacks[PeripheralCallbacks.RESPONSE_RECEIVE_SUCCESS_CALLBACK] = responseReceivedCallback
    transferHandler.sendMessage(InitTransferMessage(request.toByteArray()))
  }

  fun notifyVerificationStatus(accepted: Boolean) {
    if(accepted) {
      peripheral.sendData(SERVICE_UUID, GattService.VERIFICATION_STATUS_CHAR_UUID,
        byteArrayOf(io.mosip.tuvali.wallet.transfer.TransferHandler.VerificationStates.ACCEPTED.ordinal.toByte()))
    } else {
      peripheral.sendData(SERVICE_UUID, GattService.VERIFICATION_STATUS_CHAR_UUID,
        byteArrayOf(io.mosip.tuvali.wallet.transfer.TransferHandler.VerificationStates.REJECTED.ordinal.toByte()))
    }
  }

  override fun onAdvertisementStartSuccessful() {
    Log.d(logTag, "onAdvertisementStartSuccess")
    // Avoid spurious device connected events to be sent to higher layer before advertisement starts successfully
    val successCallback = callbacks[PeripheralCallbacks.ADV_SUCCESS_CALLBACK]
    successCallback?.let {
      callbacks[PeripheralCallbacks.DEVICE_CONNECTED_CALLBACK] = it
    }
  }

  override fun onAdvertisementStartFailed(errorCode: Int) {
    Log.e(logTag, "onAdvertisementStartFailed: $errorCode")
    // TODO: Handle error
  }

  override fun sendDataOverNotification(charUUID: UUID, data: ByteArray) {
    peripheral.sendData(SERVICE_UUID, charUUID, data)
  }

  override fun onReceivedWrite(uuid: UUID, value: ByteArray?) {
    when (uuid) {
      GattService.IDENTITY_CHARACTERISTIC_UUID -> {
        value?.let {
          // Total size of identity char value will be 12 bytes IV + 32 bytes pub key
          if (value.size < 12 + 32) {
            return
          }
          iv = value.copyOfRange(0, 12)
          walletPubKey = value.copyOfRange(12, 12 + 32)
          Log.i(
            logTag,
            "received wallet iv: ${Hex.toHexString(iv)}, wallet pub key: ${
              Hex.toHexString(
                walletPubKey
              )
            }"
          )
          secretsTranslator = verifierCryptoBox.buildSecretsTranslator(iv, walletPubKey)
          // TODO: Validate pub key, how to handle if not valid?
          responseListener("exchange-sender-info", "{\"deviceName\": \"Wallet\"}")
          peripheral.enableCommunication()
        }
      }
      GattService.SEMAPHORE_CHAR_UUID -> {
        value?.let {
          if (value.isEmpty()) {
            return
          }
          val semaphoreValue = value[0].toInt()
          if (semaphoreValue == Semaphore.SemaphoreMarker.RequestReport.ordinal) {
            val remoteRequestedTransferReportMessage =
              RemoteRequestedTransferReportMessage(semaphoreValue)
            transferHandler.sendMessage(remoteRequestedTransferReportMessage)
          } else if (semaphoreValue == Semaphore.SemaphoreMarker.Error.ordinal) {
            onResponseReceivedFailed("received error on semaphore from remote")
          }
        }
      }
      GattService.RESPONSE_SIZE_CHAR_UUID -> {
        value?.let {
          Log.d(logTag, "received response size on characteristic value: ${String(value)}")
          val responseSize: Int = String(value).toInt()
          Log.d(logTag, "received response size on characteristic: $responseSize")
          val responseSizeReadSuccessMessage = ResponseSizeReadSuccessMessage(responseSize)
          transferHandler.sendMessage(responseSizeReadSuccessMessage)
        }
      }
      GattService.RESPONSE_CHAR_UUID -> {
        if (value != null) {
          Log.d(logTag, "received response chunk on characteristic of size: ${value.size}")
          transferHandler.sendMessage(ResponseChunkReceivedMessage(value))
        }
      }
    }
  }

  override fun onSendDataNotified(uuid: UUID, isSent: Boolean) {
    when (uuid) {
      GattService.SEMAPHORE_CHAR_UUID -> {
        //TODO: Can re-send report if failed to send notification with exponential backoff
        Log.d(logTag, "notification sent status $isSent for uuid: $uuid")
      }
      GattService.VERIFICATION_STATUS_CHAR_UUID -> {
        if (transferHandler.getCurrentState() == TransferHandler.States.TransferComplete) {
          if (!isSent){
            Log.e(logTag, "onSendDataFail: Failed to notify verification status to wallet about")
          }
        }
      }
    }
  }

  override fun onDeviceConnected() {
    Log.d(logTag, "onDeviceConnected: sending event")
    val deviceConnectedCallback = callbacks[PeripheralCallbacks.DEVICE_CONNECTED_CALLBACK]
    peripheral.stopAdvertisement()

    deviceConnectedCallback?.let {
      it()
      callbacks.remove(PeripheralCallbacks.DEVICE_CONNECTED_CALLBACK)
    }
  }

  override fun onResponseReceived(data: ByteArray) {
    Log.d(logTag, "dataInBytes size: ${data.size}, sha256: ${Util.getSha256(data)}")
    val decryptedData = secretsTranslator?.decryptUponReceive(data)
    if (decryptedData != null) {
      Log.d(logTag, "decryptedData size: ${decryptedData.size}")
      val decompressedData = Util.decompress(decryptedData)
      Log.d(logTag, "decompression before: ${decryptedData.size} and after: ${decompressedData?.size}")
      responseListener("send-vc", String(decompressedData!!))
    } else {
      Log.e(logTag, "failed to decrypt data with size: ${data.size}")
      // TODO: Handle error
    }
  }

  override fun onResponseReceivedFailed(errorMsg: String) {
    Log.d(logTag, "onResponseReceiveFailed errorMsg: $errorMsg")
    // TODO: Handle error
  }

  fun getAdvIdentifier(identifier: String): String {
    // 5 bytes, since it's in hex it'd be twice
    return Hex.toHexString("${identifier}_".toByteArray() + publicKey.copyOfRange(0, 5))
  }

  private fun getAdvPayload(advIdentifier: String): ByteArray {
    // Readable Identifier from higher layer + _ + first 5 bytes of public key
    return advIdentifier.toByteArray() + "_".toByteArray() + publicKey.copyOfRange(0, 5)
  }

  private fun getScanRespPayload(): ByteArray {
    return publicKey.copyOfRange(5, 32) // should contain 27 bytes
  }
}
