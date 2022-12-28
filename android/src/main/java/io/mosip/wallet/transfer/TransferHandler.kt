package io.mosip.wallet.transfer

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import io.mosip.ble.central.Central
import io.mosip.transfer.Chunker
import io.mosip.transfer.Semaphore
import io.mosip.verifier.GattService
import io.mosip.verifier.transfer.message.ResponseTransferFailedMessage
import io.mosip.wallet.transfer.message.*
import java.util.*

class TransferHandler(looper: Looper, private val central: Central, val serviceUUID: UUID, private val transferListener: ITransferListener) :
  Handler(looper) {
  private val logTag = "TransferHandler"
  private var readCounter = 0;
  private var chunkCounter = 0;

  enum class States {
    UnInitialised,
    ResponseSizeWritePending,
    ResponseSizeWriteSuccess,
    ResponseSizeWriteFailed,
    ResponseWritePending,
    ResponseWriteFailed,
    TransferComplete,
    PendingSemaphoreAck
  }

  enum class VerificationStates {
    ACCEPTED,
    REJECTED
  }

  private var currentState: States = States.UnInitialised
  private var chunker: Chunker? = null
  private var responseStartTimeInMillis: Long = 0

  override fun handleMessage(msg: Message) {
    Log.d(logTag, "Received message to transfer thread handler: ${msg.what} and ${msg.data}")
    when (msg.what) {
      IMessage.TransferMessageTypes.INIT_RESPONSE_TRANSFER.ordinal -> {
        val initResponseTransferMessage = msg.obj as InitResponseTransferMessage
        val responseData = initResponseTransferMessage.data
        Log.d(logTag, "Total response size of data ${responseData.size}")
        chunker = Chunker(responseData)
        currentState = States.ResponseSizeWritePending
        this.sendMessage(ResponseSizeWritePendingMessage(responseData.size))
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_WRITE_PENDING.ordinal -> {
        val responseSizeWritePendingMessage = msg.obj as ResponseSizeWritePendingMessage
        sendResponseSize(responseSizeWritePendingMessage.size)
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_WRITE_SUCCESS.ordinal -> {
        responseStartTimeInMillis = System.currentTimeMillis()
        currentState = States.ResponseSizeWriteSuccess
        initResponseChunkSend()
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_WRITE_FAILED.ordinal -> {
        currentState = States.ResponseSizeWriteFailed
      }
      IMessage.TransferMessageTypes.INIT_RESPONSE_CHUNK_TRANSFER.ordinal -> {
        currentState = States.ResponseWritePending
        sendResponseChunk()
      }
      IMessage.TransferMessageTypes.READ_SEMAPHORE_STATUS.ordinal -> {
        currentState = States.PendingSemaphoreAck
        readSemaphoreStatus()
      }
      IMessage.TransferMessageTypes.CHUNK_WRITE_TO_REMOTE_STATUS_UPDATED.ordinal -> {
        val chunkWriteToRemoteStatusUpdatedMessage =
          msg.obj as ChunkWriteToRemoteStatusUpdatedMessage
        when (chunkWriteToRemoteStatusUpdatedMessage.semaphoreCharValue) {
          Semaphore.SemaphoreMarker.ProcessChunkPending.ordinal -> {
            Log.d(logTag, "Semaphore is still pending: ${readCounter++}")
            if(readCounter < 50) {
              readSemaphoreAckDelayed()
            }
          }
          Semaphore.SemaphoreMarker.FailedToRead.ordinal -> {
            Log.d(logTag, "Failed to read semaphore: ${readCounter++}")
            if(readCounter < 50) {
              readSemaphoreAckDelayed()
            }
          }
          Semaphore.SemaphoreMarker.ProcessChunkComplete.ordinal -> {
            currentState = States.ResponseWritePending
            sendResponseChunk()
          }
          Semaphore.SemaphoreMarker.ResendChunk.ordinal -> {
            Log.d(logTag, "handleMessage: resend chunk requested")
          }
          Semaphore.SemaphoreMarker.Error.ordinal -> {
            Log.d(logTag, "handleMessage: chunk marked as error while reading by remote")
            currentState = States.ResponseWriteFailed
            this.sendMessage(ResponseTransferFailureMessage("Chunk Failure"))
          }
        }
      }
      IMessage.TransferMessageTypes.RESPONSE_CHUNK_WRITE_SUCCESS.ordinal -> {
        setSemaphoreToPending()
        chunkCounter++
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE.ordinal -> {
        // TODO: Let higher level know
        Log.d(logTag, "handleMessage: Successfully transferred vc in ${System.currentTimeMillis() - responseStartTimeInMillis}ms")
        currentState = States.TransferComplete
        transferListener.onResponseSent()
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_FAILED.ordinal -> {
        val responseTransferFailedMessage = msg.obj as ResponseTransferFailedMessage
        Log.d(logTag, "handleMessage: response transfer failed")
        transferListener.onResponseSendFailure(responseTransferFailedMessage.errorMsg)
        currentState = States.ResponseSizeWriteFailed
      }
    }
  }

  private fun setSemaphoreToPending() {
    central.write(
      serviceUUID,
      GattService.SEMAPHORE_CHAR_UUID,
      byteArrayOf(Semaphore.SemaphoreMarker.ProcessChunkPending.ordinal.toByte())
    )
  }

  private fun readSemaphoreStatus() {
    central.read(serviceUUID, GattService.SEMAPHORE_CHAR_UUID)
  }

  private fun sendResponseChunk() {
    Log.d(logTag, "Writing Chunk: $chunkCounter and is complete: ${chunker?.isComplete()}")
    if (chunker?.isComplete() == true) {
      this.sendMessage(ResponseTransferCompleteMessage())
      return
    }

    val chunkArray = chunker?.next()
    if (chunkArray != null) {
      central.write(
        serviceUUID,
        GattService.RESPONSE_CHAR_UUID,
        byteArrayOf(0, 1, 73, Byte.MAX_VALUE) + chunkArray
      )

      readCounter = 0
    }
  }

  fun readSemaphoreAckDelayed() {
    this.sendMessageDelayed(ReadSemaphoreStatusMessage(), 5)
  }

  private fun initResponseChunkSend() {
    Log.d(logTag, "initResponseChunkSend")
    val initResponseChunkTransferMessage = InitResponseChunkTransferMessage()
    this.sendMessage(initResponseChunkTransferMessage)
  }

  fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.msgType.ordinal
    message.obj = msg
    this.sendMessage(message)
  }


  private fun sendMessageDelayed(msg: IMessage, delayMillis: Long) {
    val message = this.obtainMessage()
    message.what = msg.msgType.ordinal
    message.obj = msg
    this.sendMessageDelayed(message, delayMillis)
  }

  private fun sendResponseSize(size: Int) {
    central.write(
      serviceUUID,
      GattService.RESPONSE_SIZE_CHAR_UUID,
      "$size".toByteArray()
    )
  }

  fun getCurrentState(): States {
    return currentState
  }
}
