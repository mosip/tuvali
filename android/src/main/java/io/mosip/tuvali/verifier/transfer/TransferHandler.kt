package io.mosip.tuvali.verifier.transfer

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import io.mosip.tuvali.ble.peripheral.Peripheral
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import io.mosip.tuvali.transfer.*
import io.mosip.tuvali.verifier.GattService
import io.mosip.tuvali.verifier.exception.CorruptedChunkReceivedException
import io.mosip.tuvali.verifier.exception.TooManyFailureChunksException
import io.mosip.tuvali.verifier.exception.VerifierTransferHandlerException
import io.mosip.tuvali.verifier.transfer.message.*
import java.util.*

class TransferHandler(looper: Looper, private val peripheral: Peripheral, private val transferListener: ITransferListener, val serviceUUID: UUID) : Handler(looper) {
  private val logTag = getLogTag(javaClass.simpleName)
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
  private var assembler: Assembler? = null
  private var responseStartTimeInMillis: Long = 0
  private val defaultTransferReportPageSize = 90

  override fun handleMessage(msg: Message) {
    when(msg.what) {
      IMessage.TransferMessageTypes.RESPONSE_SIZE_READ.ordinal -> {
        responseStartTimeInMillis = System.currentTimeMillis()
        val responseSizeReadSuccessMessage = msg.obj as ResponseSizeReadSuccessMessage
        val maxChunkSize = responseSizeReadSuccessMessage.maxDataBytes

        try {
          Log.d(logTag, "MTU size used for assembler: $maxChunkSize bytes")
          assembler = Assembler(responseSizeReadSuccessMessage.responseSize, maxChunkSize)
        } catch (c: CorruptedChunkReceivedException) {
          this.sendMessage(ResponseTransferFailedMessage("Corrupted Data from Remote " + c.message.toString()))
          return
        }
        currentState = States.ResponseReadPending
      }
      // On verifier side, we can wait on response char, instead of transfer report request to know when chunk arrived
      IMessage.TransferMessageTypes.RESPONSE_CHUNK_RECEIVED.ordinal -> {
        val responseChunkReceivedMessage = msg.obj as ResponseChunkReceivedMessage
        assembleChunk(responseChunkReceivedMessage.chunkData)
      }
      IMessage.TransferMessageTypes.REMOTE_REQUESTED_TRANSFER_REPORT.ordinal -> {
        val remoteRequestedTransferReportMessage = msg.obj as RemoteRequestedTransferReportMessage
        val maxDataBytes = remoteRequestedTransferReportMessage.maxDataBytes
        when(remoteRequestedTransferReportMessage.transferReportRequestCharValue) {
          TransferReportRequest.ReportType.RequestReport.ordinal -> {
            handleTransferReportRequest(maxDataBytes)
          }
          TransferReportRequest.ReportType.Error.ordinal -> {
            transferListener.onResponseReceivedFailed("received error on transfer summary request char from remote")
          }
          else -> {
            transferListener.onResponseReceivedFailed("unknown value received on transfer summary request char from remote: ${remoteRequestedTransferReportMessage.transferReportRequestCharValue}")
          }
        }
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE.ordinal -> {
        Log.i(logTag, "response transfer complete in ${System.currentTimeMillis() - responseStartTimeInMillis}ms")
        val responseTransferCompleteMessage = msg.obj as ResponseTransferCompleteMessage
        transferListener.onResponseReceived(responseTransferCompleteMessage.data)
        currentState = States.TransferComplete
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_FAILED.ordinal -> {
        val responseTransferFailedMessage = msg.obj as ResponseTransferFailedMessage
        Log.d(logTag, "handleMessage: response transfer failed with errMsg: ${responseTransferFailedMessage.errorMsg}")
        transferListener.onResponseReceivedFailed(responseTransferFailedMessage.errorMsg)
        currentState = States.ResponseReadFailed
      }
    }
  }

  private fun handleTransferReportRequest(maxDataBytes: Int) {
    if (assembler?.isComplete() == true) {
      Log.d(logTag, "success frame: transfer completed")
      val transferReport = TransferReport(TransferReport.ReportType.SUCCESS, intArrayOf(), maxDataBytes)
      transferListener.sendDataOverNotification(
        GattService.TRANSFER_REPORT_RESPONSE_CHAR_UUID,
        transferReport.toByteArray()
      )
      this.sendMessage(ResponseTransferCompleteMessage(assembler?.data()!!))
      return
    }

    val missedSequenceNumbers = assembler?.getMissedSequenceNumbers()
    val missedCount = missedSequenceNumbers?.size

    if(assembler != null && missedCount!! > (0.7 * assembler!!.totalChunkCount)) {
      throw TooManyFailureChunksException("Failing VC transfer as failure chunks are more than 70% of total chunks")
    }

    val transferReport =TransferReport(
      TransferReport.ReportType.MISSING_CHUNKS,
      missedSequenceNumbers!!,
      maxDataBytes
    )

    transferListener.sendDataOverNotification(
      GattService.TRANSFER_REPORT_RESPONSE_CHAR_UUID,
      transferReport.toByteArray()
    )
  }

  private fun assembleChunk(chunkData: ByteArray) {
    if (assembler?.isComplete() == true) {
      return
    }
    //Log.d(logTag, "SequenceNumber: ${Util.byteArrayToInt(chunkData.copyOfRange(0,2), TwoBytes)},  Sha256: ${Util.getSha256(chunkData)}")
    assembler?.addChunk(chunkData)
  }

  override fun dispatchMessage(msg: Message) {
    try {
      super.dispatchMessage(msg)
    } catch (e: Exception) {
      transferListener.onException(VerifierTransferHandlerException("Exception in Verifier Transfer Handler", e))
      Log.e(logTag, "dispatchMessage " + e.message)
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
