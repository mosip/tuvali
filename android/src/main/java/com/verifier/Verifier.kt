package com.verifier

import android.content.Context
import android.os.HandlerThread
import android.os.Process.THREAD_PRIORITY_DEFAULT
import android.util.Log
import com.ble.peripheral.IPeripheralListener
import com.ble.peripheral.Peripheral
import com.facebook.react.bridge.Callback
<<<<<<< HEAD
import com.verifier.transfer.ITransferListener
import com.verifier.transfer.TransferHandler
import com.verifier.transfer.message.*
=======
import java.nio.charset.Charset
>>>>>>> b960051 (chore(#305): (wip) Write wallet's PK  to verifier)
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
class Verifier(context: Context, private val responseListener: (String, String) -> Unit) :
  IPeripheralListener, ITransferListener {
  private val logTag = "Verifier"
  private var publicKey: String = "b0f8980279d4df9f383bfd6e990b45c5fcba1c4fbef76c27b9141dff50b97983"
  private lateinit var walletPubKey: String
  private lateinit var iv: String
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
    RESPONSE_RECEIVED_CALLBACK
  }

  private val callbacks = mutableMapOf<PeripheralCallbacks, Callback>()

  init {
    peripheral = Peripheral(context, this@Verifier)
    val gattService = GattService()
    peripheral.setupService(gattService.create())
    transferHandler = TransferHandler(handlerThread.looper, peripheral, SERVICE_UUID)
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

  fun sendRequest(request: String, responseReceivedCallback: Callback) {
    callbacks[PeripheralCallbacks.RESPONSE_RECEIVED_CALLBACK] = responseReceivedCallback
    transferHandler.sendMessage(InitTransferMessage(request.toByteArray().toUByteArray()))
  }

  override fun onAdvertisementStartSuccessful() {
    Log.d(logTag, "onAdvertisementStartSuccess")
    val successCallback = callbacks[PeripheralCallbacks.ADV_SUCCESS_CALLBACK]
    successCallback?.let { it() }
  }

  override fun onAdvertisementStartFailed(errorCode: Int) {
    Log.e(logTag, "onAdvertisementStartFailed: $errorCode")
  }

  override fun onReceivedWrite(uuid: UUID, value: ByteArray?) {
    when (uuid) {
        GattService.IDENTITY_CHARACTERISTIC_UUID -> {
          val identityValue = value!!.decodeToString()
          var identitySubstrings = listOf<String>()
          if (identityValue !== "") {
            identitySubstrings = identityValue.split("_", limit = 2)
          }
          if (identitySubstrings.size > 1) {
            iv = identitySubstrings[0]
            walletPubKey = identitySubstrings[1]
          }
          // TODO: Validate pub key, how to handle if not valid?
          if (walletPubKey != "") {
            responseListener("exchange-sender-info", "{\"deviceName\": \"Wallet\"}")
            peripheral.enableCommunication()
          }
        }
        GattService.SEMAPHORE_CHAR_UUID -> {
          val semaphoreValue = value.toString().toInt()
          val chunkReadByRemoteStatusUpdatedMessage =
            ChunkReadByRemoteStatusUpdatedMessage(semaphoreValue)
          transferHandler.sendMessage(chunkReadByRemoteStatusUpdatedMessage)
        }
        GattService.RESPONSE_SIZE_CHAR_UUID -> {
          val responseSize = value.toString().toInt()
          val responseSizeReadSuccessMessage = ResponseSizeReadSuccessMessage(responseSize)
          transferHandler.sendMessage(responseSizeReadSuccessMessage)
        }
        GattService.RESPONSE_CHAR_UUID -> {
          if (value != null) {
            transferHandler.sendMessage(ResponseChunkReadMessage(value.toUByteArray()))
          }
        }
    }
  }

  //TODO: Remove if not needed
  override fun onRead(uuid: UUID?, read: Boolean) {
    Log.d(logTag, "onRead: called, does nothing")
  }

  override fun onSendDataNotified(uuid: UUID, notificationTriggered: Boolean) {
    when (uuid) {
      GattService.SEMAPHORE_CHAR_UUID -> {
        if (transferHandler.getCurrentState() == TransferHandler.States.RequestWritePending) {
          //TODO: Handle this?
        }
      }
      GattService.REQUEST_SIZE_CHAR_UUID -> {
        if (transferHandler.getCurrentState() == TransferHandler.States.RequestSizeWritePending) {
          if (notificationTriggered) {
            transferHandler.sendMessage(RequestSizeWriteSuccessMessage())
          } else {
            transferHandler.sendMessage(RequestSizeWriteFailedMessage("notifying request size write to remote failed"))
          }
        } else {
          Log.e(logTag, "onSendDataSuccessful: on unknown state of transfer handler: ${transferHandler.getCurrentState()}")
        }
      }
      GattService.REQUEST_CHAR_UUID -> {
        if (transferHandler.getCurrentState() == TransferHandler.States.RequestWritePending) {
          transferHandler.sendMessage(RequestChunkWriteFailedMessage("notifying chunk write to remote failed"))
        }
      }
    }
  }

  // TODO: Can remove this
  override fun onDeviceConnected() {
    Log.d(logTag, "onDeviceConnected: sending event")
    responseListener("exchange-sender-info", "{\"deviceName\": \"Wallet\"}")
  }

  override fun onResponseReceived(data: UByteArray) {
    Log.d(logTag, "onResponseReceived data: $data")
    val responseReceivedCallback = callbacks[PeripheralCallbacks.RESPONSE_RECEIVED_CALLBACK]
    responseReceivedCallback?.let { it() }
  }

  private fun getAdvPayload(advIdentifier: String): String {
    return advIdentifier + "_" + publicKey.substring(0, 5)
  }

  private fun getScanRespPayload(): String {
    return publicKey.substring(5, 32)
  }
}
