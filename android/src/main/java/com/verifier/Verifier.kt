package com.verifier

import android.content.Context
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_DEFAULT
import android.util.Log
import com.ble.peripheral.IPeripheralListener
import com.ble.peripheral.Peripheral
import com.cryptography.SecretsTranslator
import com.cryptography.VerifierCryptoBox
import com.cryptography.VerifierCryptoBoxBuilder
import com.facebook.common.util.Hex
import com.facebook.react.bridge.Callback
import com.transfer.Semaphore
import com.verifier.transfer.ITransferListener
import com.verifier.transfer.TransferHandler
import com.verifier.transfer.message.*
import java.security.SecureRandom
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
class Verifier(context: Context, private val responseListener: (String, String) -> Unit) :
  IPeripheralListener, ITransferListener {
  private var secretsTranslator: SecretsTranslator? = null;
  private val logTag = "Verifier"
  private var publicKey: ByteArray = byteArrayOf()
  private lateinit var walletPubKey: ByteArray
  private lateinit var iv: ByteArray
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

  fun generateKeyPair() {
    // DOUBT: Should it be generated each time?
    val sr = SecureRandom()
    val vcb: VerifierCryptoBox = VerifierCryptoBoxBuilder.build(sr)
    publicKey = vcb.publicKey()
    Log.i(logTag, "Verifier public key: ${Hex.encodeHex(publicKey, false)}")
  }

  fun startAdvertisement(advIdentifier: String, successCallback: Callback) {
    callbacks[PeripheralCallbacks.DEVICE_CONNECTED_CALLBACK] = successCallback
    peripheral.start(
      SERVICE_UUID,
      SCAN_RESPONSE_SERVICE_UUID,
      getAdvPayload(advIdentifier),
      getScanRespPayload()
    )
  }

  fun sendRequest(request: String, responseReceivedCallback: Callback) {
    callbacks[PeripheralCallbacks.RESPONSE_RECEIVE_SUCCESS_CALLBACK] = responseReceivedCallback
    transferHandler.sendMessage(InitTransferMessage(request.toByteArray().toUByteArray()))
  }

  override fun onAdvertisementStartSuccessful() {
    Log.d(logTag, "onAdvertisementStartSuccess")
    val successCallback = callbacks[PeripheralCallbacks.ADV_SUCCESS_CALLBACK]
    successCallback?.let {
      it()
      callbacks.remove(PeripheralCallbacks.ADV_SUCCESS_CALLBACK)
    }
  }

  override fun onAdvertisementStartFailed(errorCode: Int) {
    Log.e(logTag, "onAdvertisementStartFailed: $errorCode")
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
            "received wallet iv: ${Hex.encodeHex(iv, false)}, wallet pub key: ${
              Hex.encodeHex(
                walletPubKey,
                false
              )
            }"
          )
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
          if(semaphoreValue == Semaphore.SemaphoreMarker.ProcessChunkPending.ordinal) {
            val chunkWroteByRemoteStatusUpdatedMessage = ChunkWroteByRemoteStatusUpdatedMessage(semaphoreValue)
            transferHandler.sendMessage(chunkWroteByRemoteStatusUpdatedMessage)
          } else if (semaphoreValue == Semaphore.SemaphoreMarker.ProcessChunkComplete.ordinal){
            val chunkReadByRemoteStatusUpdatedMessage = ChunkReadByRemoteStatusUpdatedMessage(semaphoreValue)
            transferHandler.sendMessage(chunkReadByRemoteStatusUpdatedMessage)
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
          Log.d(logTag, "received response chunk on characteristic: $value")
          transferHandler.sendMessage(ResponseChunkReceivedMessage(value.toUByteArray()))
        }
      }
    }
  }

  //TODO: Remove if not needed
  override fun onRead(uuid: UUID?, read: Boolean) {
    Log.d(logTag, "onRead: called, does nothing")
  }

  override fun onSendDataNotified(uuid: UUID, isSent: Boolean) {
    when (uuid) {
      GattService.SEMAPHORE_CHAR_UUID -> {
        if (transferHandler.getCurrentState() == TransferHandler.States.ResponseReadPending) {
          if (isSent) {
            Log.d(logTag, "Value was written to semaphore")
          } else {
            Log.d(logTag, "Failed to write value to semaphore")
          }
        }
      }
      GattService.REQUEST_SIZE_CHAR_UUID -> {
        if (transferHandler.getCurrentState() == TransferHandler.States.RequestSizeWritePending) {
          if (isSent) {
            transferHandler.sendMessage(RequestSizeWriteSuccessMessage())
          } else {
            transferHandler.sendMessage(RequestSizeWriteFailedMessage("notifying request size write to remote failed"))
          }
        } else {
          Log.e(
            logTag,
            "onSendDataSuccessful: on unknown state of transfer handler: ${transferHandler.getCurrentState()}"
          )
        }
      }
      GattService.REQUEST_CHAR_UUID -> {
        if (transferHandler.getCurrentState() == TransferHandler.States.RequestWritePending) {
          if (isSent) {
            transferHandler.sendMessage(RequestChunkWriteSuccessMessage())
          } else {
            transferHandler.sendMessage(RequestChunkWriteFailedMessage("notifying chunk write to remote failed"))
          }
        }
      }
    }
  }

  // TODO: Can remove this
  override fun onDeviceConnected() {
    Log.d(logTag, "onDeviceConnected: sending event")
    val deviceConnectedCallback = callbacks[PeripheralCallbacks.DEVICE_CONNECTED_CALLBACK]

    deviceConnectedCallback?.let {
      it()
      callbacks.remove(PeripheralCallbacks.DEVICE_CONNECTED_CALLBACK)
    }
  }

  override fun onResponseReceived(data: UByteArray) {
    Log.d(logTag, "onResponseReceived data: $data")
    responseListener("send-vc", "{\"vc\": \"${String(data.toByteArray())}\"}")
  }

  override fun onResponseReceivedFailed(errorMsg: String) {
    Log.d(logTag, "onResponseReceiveFailed errorMsg: $errorMsg")
  }

  fun getAdvIdentifier(identifier: String): String {
    // 5 bytes, since it's in hex it'd be twice
    return Hex.encodeHex("${identifier}_".toByteArray() + publicKey.copyOfRange(0, 5), false)
  }

  private fun getAdvPayload(advIdentifier: String): ByteArray {
    // Readable Identifier from higher layer + _ + first 5 bytes of public key
    return advIdentifier.toByteArray() + "_".toByteArray() + publicKey.copyOfRange(0, 5)
  }

  private fun getScanRespPayload(): ByteArray {
    return publicKey.copyOfRange(5, 32) // should contain 27 bytes
  }
}
