package com.wallet.transfer

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.central.Central
import com.verifier.GattService
import com.verifier.transfer.Chunker
import com.verifier.transfer.TransferHandler
import com.verifier.transfer.message.*
import com.wallet.transfer.message.*
import com.wallet.transfer.message.IMessage
import com.wallet.transfer.message.ResponseTransferCompleteMessage
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
class TransferHandler(looper: Looper, private val central: Central, val serviceUUID: UUID) : Handler(looper) {
  private val logTag = "TransferHandler"
  enum class States {
    UnInitialised,
    ResponseSizeWritePending,
    ResponseSizeWriteSuccess,
    ResponseSizeWriteFailed,
    ResponseWritePending,
    ResponseWriteFailed,
    TransferComplete
  }

  enum class SemaphoreMarker {
    UnInitialised,
    ProcessChunkPending,
    ProcessChunkComplete,
    ResendChunk,
    Error
  }

  private var currentState: States = States.UnInitialised
  private var responseData: UByteArray = ubyteArrayOf()
  private var chunker: Chunker? = null

  override fun handleMessage(msg: Message) {
    Log.d(logTag, "Received message to transfer thread handler: $msg")

    when(msg.what) {
      IMessage.TransferMessageTypes.INIT_RESPONSE_TRANSFER.ordinal -> {
        val initResponseTransferMessage = msg.obj as InitResponseTransferMessage
        responseData = initResponseTransferMessage.data.toUByteArray()
        chunker = Chunker(responseData)
        currentState = States.ResponseSizeWritePending
        this.sendMessage(ResponseSizeWritePendingMessage(responseData.size))
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_WRITE_PENDING.ordinal -> {
        val responseSizeWritePendingMessage = msg.obj as ResponseSizeWritePendingMessage
        sendResponseSize(responseSizeWritePendingMessage.size)
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_WRITE_SUCCESS.ordinal -> {
        currentState = States.ResponseSizeWriteSuccess
        initResponseChunkSend()
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_WRITE_FAILED.ordinal -> {
        currentState = States.ResponseSizeWriteFailed
      }
      IMessage.TransferMessageTypes.INIT_RESPONSE_CHUNK_TRANSFER.ordinal -> {
        currentState = States.ResponseWritePending
        sendResponse()
      }
      IMessage.TransferMessageTypes.CHUNK_WRITE_TO_REMOTE_STATUS_UPDATED.ordinal -> {
        val chunkReadByRemoteStatusUpdatedMessage = msg.obj as ChunkReadByRemoteStatusUpdatedMessage
        when(chunkReadByRemoteStatusUpdatedMessage.semaphoreCharValue) {
          TransferHandler.SemaphoreMarker.ProcessChunkPending.ordinal -> {
          }
          TransferHandler.SemaphoreMarker.ProcessChunkComplete.ordinal -> {
            this.sendMessage(ResponseChunkWriteSuccessMessage())
          }
          TransferHandler.SemaphoreMarker.ResendChunk.ordinal -> {
            Log.d(logTag, "handleMessage: resend chunk requested")
          }
          TransferHandler.SemaphoreMarker.Error.ordinal -> {
            Log.d(logTag, "handleMessage: chunk marked as error while reading by remote")
            currentState = States.ResponseWriteFailed
          }
        }
      }
      IMessage.TransferMessageTypes.RESPONSE_CHUNK_WRITE_SUCCESS.ordinal -> {
        sendResponse()
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE.ordinal -> {
        // TODO: Let higher level know
        currentState = States.TransferComplete
      }
    }
  }

  private fun sendResponse() {
    if (chunker?.isComplete() == true) {
      this.sendMessage(ResponseTransferCompleteMessage())
      return
    }
    val chunkArray = chunker?.next()
    if (chunkArray != null) {
      central.write(
        serviceUUID,
        GattService.RESPONSE_CHAR_UUID,
        chunkArray.toByteArray()
      )
      this.sendMessage(ChunkWriteToRemoteStatusUpdatedMessage(TransferHandler.SemaphoreMarker.ProcessChunkPending.ordinal))
    }
  }

  private fun initResponseChunkSend() {
    val initResponseChunkTransferMessage = InitResponseChunkTransferMessage()
    this.sendMessage(initResponseChunkTransferMessage)
  }

  fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.msgType.ordinal
    message.obj = msg
    this.sendMessage(message)
  }

  private fun sendResponseSize(size: Int) {
    central.write(
      serviceUUID,
      GattService.REQUEST_SIZE_CHAR_UUID,
      ByteArray(size)
    )
  }

  fun getCurrentState(): States {
    return currentState
  }
}
