package com.verifier.transfer

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.ble.peripheral.Peripheral
import com.transfer.Assembler
import com.transfer.Chunker
import com.transfer.Semaphore
import com.verifier.GattService
import com.verifier.exception.CorruptedChunkReceivedException
import com.verifier.transfer.message.*
import java.util.*

@OptIn(ExperimentalUnsignedTypes::class)
class TransferHandler(looper: Looper, private val peripheral: Peripheral, private val transferListener: ITransferListener, val serviceUUID: UUID) : Handler(looper) {
  private val logTag = "TransferHandler"
  enum class States {
    UnInitialised,
    RequestSizeWritePending,
    RequestSizeWriteSuccess,
    RequestSizeWriteFailed,
    RequestWritePending,
    RequestWriteFailed,
    ResponseSizeReadPending,
    ResponseReadPending,
    ResponseReadFailed,
    TransferComplete
  }

  private var currentState: States = States.UnInitialised
  private var requestData: UByteArray = ubyteArrayOf()
  private var chunker: Chunker? = null
  private var assembler: Assembler? = null

  override fun handleMessage(msg: Message) {
    when(msg.what) {
      IMessage.TransferMessageTypes.INIT_REQUEST_TRANSFER.ordinal -> {
        val initTransferMessage = msg.obj as InitTransferMessage
        requestData = initTransferMessage.data
        chunker = Chunker(requestData)
        currentState = States.RequestSizeWritePending
        this.sendMessage(RequestSizeWritePendingMessage(requestData.size))
      }
      IMessage.TransferMessageTypes.REQUEST_SIZE_WRITE_PENDING.ordinal -> {
        val requestSizeWritePendingMessage = msg.obj as RequestSizeWritePendingMessage
        sendRequestSize(requestSizeWritePendingMessage.size)
      }
      IMessage.TransferMessageTypes.REQUEST_SIZE_WRITE_SUCCESS.ordinal -> {
        Log.d(logTag, "handleMessage: request size write success")
        currentState = States.RequestSizeWriteSuccess
        initRequestChunkSend()
      }
      IMessage.TransferMessageTypes.REQUEST_SIZE_WRITE_FAILED.ordinal -> {
        val requestSizeWriteFailedMessage = msg.obj as RequestSizeWriteFailedMessage
        Log.e(logTag, "handleMessage: request size write failed with error: ${requestSizeWriteFailedMessage.errorMsg}")
        currentState = States.RequestSizeWriteFailed
      }
      IMessage.TransferMessageTypes.INIT_REQUEST_CHUNK_TRANSFER.ordinal -> {
        sendRequestChunk()
        currentState = States.RequestWritePending
      }
      IMessage.TransferMessageTypes.CHUNK_READ_BY_REMOTE_STATUS_UPDATED.ordinal -> {
        val chunkReadByRemoteStatusUpdatedMessage = msg.obj as ChunkReadByRemoteStatusUpdatedMessage
        when(chunkReadByRemoteStatusUpdatedMessage.semaphoreCharValue) {
          Semaphore.SemaphoreMarker.ProcessChunkPending.ordinal -> {
            markChunkSend()
          }
          Semaphore.SemaphoreMarker.ProcessChunkComplete.ordinal -> {
            this.sendMessage(RequestChunkWriteSuccessMessage())
          }
          Semaphore.SemaphoreMarker.ResendChunk.ordinal -> {
            Log.d(logTag, "handleMessage: resend chunk requested")
          }
          Semaphore.SemaphoreMarker.Error.ordinal -> {
            Log.d(logTag, "handleMessage: chunk marked as error while reading by remote")
          }
        }
      }
      IMessage.TransferMessageTypes.REQUEST_CHUNK_WRITE_SUCCESS.ordinal -> {
        sendRequestChunk()
      }
      IMessage.TransferMessageTypes.REQUEST_CHUNK_WRITE_FAILED.ordinal -> {
        val requestChunkWriteFailedMessage = msg.obj as RequestChunkWriteFailedMessage
        Log.e(logTag, "request chunk write to remote failed: ${requestChunkWriteFailedMessage.errorMsg}")
        currentState = States.RequestWriteFailed
      }
      IMessage.TransferMessageTypes.REQUEST_TRANSFER_COMPLETE.ordinal -> {
        markChunkTransferComplete()
        currentState = States.ResponseSizeReadPending
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_READ.ordinal -> {
        val responseSizeReadSuccessMessage = msg.obj as ResponseSizeReadSuccessMessage
        try {
          assembler = Assembler(responseSizeReadSuccessMessage.responseSize)
        } catch (c: CorruptedChunkReceivedException) {
          this.sendMessage(ResponseTransferFailedMessage("Corrupted Data from Remote " + c.message.toString()))
          return
        }
        currentState = States.ResponseReadPending
      }
      // On verifier side, we can wait on response char, instead of semaphore to know when chunk arrived
      IMessage.TransferMessageTypes.RESPONSE_CHUNK_READ.ordinal -> {
        val responseChunkReadMessage = msg.obj as ResponseChunkReadMessage
        assembleChunk(responseChunkReadMessage.chunkData)
      }
      IMessage.TransferMessageTypes.UPDATE_CHUNK_READ_STATUS_TO_REMOTE.ordinal -> {
        val updateChunkReceivedStatusToRemoteMessage =
          msg.obj as UpdateChunkReceivedStatusToRemoteMessage
        when(updateChunkReceivedStatusToRemoteMessage.semaphoreCharValue) {
          Semaphore.SemaphoreMarker.ProcessChunkComplete.ordinal -> markChunkReceive()
          Semaphore.SemaphoreMarker.ResendChunk.ordinal -> Log.e(logTag, "receive semaphore value to re-read")
        }
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE.ordinal -> {
        val responseTransferCompleteMessage = msg.obj as ResponseTransferCompleteMessage
        Log.d(logTag, "handleMessage: response transfer complete")
        transferListener.onResponseReceived(responseTransferCompleteMessage.data)
        currentState = States.TransferComplete
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_FAILED.ordinal -> {
        val responseTransferFailedMessage = msg.obj as ResponseTransferFailedMessage
        Log.d(logTag, "handleMessage: response transfer failed")
        transferListener.onResponseReceivedFailed(responseTransferFailedMessage.errorMsg)
        currentState = States.ResponseReadFailed
      }
    }
  }

  private fun markChunkSend() {
    peripheral.sendData(
      serviceUUID,
      GattService.SEMAPHORE_CHAR_UUID,
      ubyteArrayOf(Semaphore.SemaphoreMarker.ProcessChunkPending.ordinal.toUByte())
    )
  }

  private fun markChunkReceive() {
    peripheral.sendData(
      serviceUUID,
      GattService.SEMAPHORE_CHAR_UUID,
      ubyteArrayOf(Semaphore.SemaphoreMarker.ProcessChunkComplete.ordinal.toUByte())
    )
  }

  private fun markChunkTransferComplete() {
    peripheral.sendData(
      serviceUUID,
      GattService.SEMAPHORE_CHAR_UUID,
      ubyteArrayOf(Semaphore.SemaphoreMarker.UnInitialised.ordinal.toUByte())
    )
  }

  private fun sendRequestChunk() {
    if (chunker?.isComplete() == true) {
      this.sendMessage(RequestTransferCompleteMessage())
      return
    }
    val chunkArray = chunker?.next()
    if (chunkArray != null) {
      peripheral.sendData(
        serviceUUID,
        GattService.REQUEST_CHAR_UUID,
        chunkArray
      )
      this.sendMessage(ChunkReadByRemoteStatusUpdatedMessage(Semaphore.SemaphoreMarker.ProcessChunkPending.ordinal))
    }
  }

  private fun assembleChunk(chunkData: UByteArray) {
    if (assembler?.isComplete() == true) {
      return
    }
    assembler?.addChunk(chunkData)
    this.sendMessage(UpdateChunkReceivedStatusToRemoteMessage(Semaphore.SemaphoreMarker.ProcessChunkComplete.ordinal))

    if (assembler?.isComplete() == true) {
      if (assembler?.data() == null){
        return this.sendMessage(ResponseTransferFailedMessage("assembler is complete data is null"))
      }
      this.sendMessage(ResponseTransferCompleteMessage(assembler?.data()!!))
    }
  }

  private fun initRequestChunkSend() {
    val initRequestChunkTransferMessage =
      InitRequestChunkTransferMessage()
    this.sendMessage(initRequestChunkTransferMessage)
  }

  private fun sendRequestSize(size: Int) {
    peripheral.sendData(
      serviceUUID,
      GattService.REQUEST_SIZE_CHAR_UUID,
      arrayOf(size.toUByte()).toUByteArray()
    )
  }

  fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.msgType.ordinal
    message.obj = msg
    this.sendMessage(message)
  }

  fun getCurrentState(): States {
    return currentState
  }
}
