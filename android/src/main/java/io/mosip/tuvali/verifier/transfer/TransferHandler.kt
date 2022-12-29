package io.mosip.tuvali.verifier.transfer

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import io.mosip.tuvali.ble.peripheral.Peripheral
import io.mosip.tuvali.transfer.Assembler
import io.mosip.tuvali.transfer.Chunker
import io.mosip.tuvali.transfer.Semaphore
import io.mosip.tuvali.verifier.exception.CorruptedChunkReceivedException
import io.mosip.tuvali.verifier.transfer.message.*
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

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

  private var currentState: States = States.ResponseSizeReadPending
  private var requestData: ByteArray = byteArrayOf()
  private var chunker: Chunker? = null
  private var assembler: Assembler? = null
  private var semaphoreWriteAtomic: AtomicInteger = AtomicInteger(Semaphore.SemaphoreMarker.UnInitialised.ordinal)
  private var responseStartTimeInMillis: Long = 0

  override fun handleMessage(msg: Message) {
    when(msg.what) {
      IMessage.TransferMessageTypes.REMOTE_REQUESTED_TRANSFER_REPORT.ordinal -> {
        val remoteRequestedTransferReportMessage = msg.obj as RemoteRequestedTransferReportMessage
        when(remoteRequestedTransferReportMessage.semaphoreCharValue) {
          Semaphore.SemaphoreMarker.RequestReport.ordinal -> {
            //TODO: Handle sending transfer report to remote
          }
        }
      }
      IMessage.TransferMessageTypes.RESPONSE_SIZE_READ.ordinal -> {
        responseStartTimeInMillis = System.currentTimeMillis()
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
      IMessage.TransferMessageTypes.RESPONSE_CHUNK_RECEIVED.ordinal -> {
        val responseChunkReceivedMessage = msg.obj as ResponseChunkReceivedMessage
        assembleChunk(responseChunkReceivedMessage.chunkData)
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE.ordinal -> {
        Log.d(logTag, "response transfer complete in ${System.currentTimeMillis() - responseStartTimeInMillis}ms")
        val responseTransferCompleteMessage = msg.obj as ResponseTransferCompleteMessage
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

  private fun assembleChunk(chunkData: ByteArray) {
    if (assembler?.isComplete() == true) {
      return
    }
    assembler?.addChunk(chunkData)
//    this.sendMessage(UpdateChunkReceivedStatusToRemoteMessage(Semaphore.SemaphoreMarker.ProcessChunkComplete.ordinal))

    if (assembler?.isComplete() == true) {
      if (assembler?.data() == null){
        return this.sendMessage(ResponseTransferFailedMessage("assembler is complete data is null"))
      }
      this.sendMessage(ResponseTransferCompleteMessage(assembler?.data()!!))
    }
  }

  fun sendMessage(msg: IMessage) {
    val message = this.obtainMessage()
    message.what = msg.msgType.ordinal
    message.obj = msg
    this.sendMessage(message)
  }

  fun sendMessageDelayed(msg: IMessage, delayInMillis: Long) {
    val message = this.obtainMessage()
    message.what = msg.msgType.ordinal
    message.obj = msg
    this.sendMessageDelayed(message,delayInMillis)
  }

  fun getCurrentState(): States {
    return currentState
  }
}
