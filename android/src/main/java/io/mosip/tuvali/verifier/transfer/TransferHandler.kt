package io.mosip.tuvali.verifier.transfer

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import io.mosip.tuvali.ble.peripheral.Peripheral
import io.mosip.tuvali.openid4vpble.exception.exception.TransferHandlerException
import io.mosip.tuvali.transfer.Assembler
import io.mosip.tuvali.transfer.TransferReportRequest
import io.mosip.tuvali.transfer.TransferReport
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.verifier.GattService
import io.mosip.tuvali.verifier.exception.CorruptedChunkReceivedException
import io.mosip.tuvali.verifier.exception.TooManyFailureChunksException
import io.mosip.tuvali.verifier.transfer.message.*
import java.util.*
import kotlin.math.ceil
import kotlin.math.min

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
  private var assembler: Assembler? = null
  private var responseStartTimeInMillis: Long = 0
  private val defaultTransferReportPageSize = 90

  override fun handleMessage(msg: Message) {
    when(msg.what) {
      IMessage.TransferMessageTypes.RESPONSE_SIZE_READ.ordinal -> {
        responseStartTimeInMillis = System.currentTimeMillis()
        val responseSizeReadSuccessMessage = msg.obj as ResponseSizeReadSuccessMessage
        val mtuSize = responseSizeReadSuccessMessage.mtuSize

        try {
          Log.d(logTag, "MTU size used for assembler: $mtuSize bytes")
          assembler = Assembler(responseSizeReadSuccessMessage.responseSize, mtuSize)
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
        when(remoteRequestedTransferReportMessage.transferReportRequestCharValue) {
          TransferReportRequest.ReportType.RequestReport.ordinal -> {
            handleTransferReportRequest()
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

  private fun handleTransferReportRequest() {
    if (assembler?.isComplete() == true) {
      Log.d(logTag, "success frame: transfer completed")
      val transferReport = TransferReport(TransferReport.ReportType.SUCCESS, 0, null)
      transferListener.sendDataOverNotification(
        GattService.TRANSFER_REPORT_RESPONSE_CHAR_UUID,
        transferReport.toByteArray()
      )
      this.sendMessage(ResponseTransferCompleteMessage(assembler?.data()!!))
      return
    }

    val missedSequenceNumbers = assembler?.getMissedSequenceNumbers()
    val missedCount = missedSequenceNumbers?.size
    val totalPages: Double = ceil(missedCount!!.toDouble() / defaultTransferReportPageSize)

    Log.d(
      logTag,
      "failure frame: missedChunksCount: $missedCount, defaultTransferReportPageSize: $defaultTransferReportPageSize, totalPages: $totalPages"
    )

    if(assembler != null && missedCount > (0.7 * assembler!!.totalChunkCount)) {
      throw TooManyFailureChunksException("Failing VC transfer as failure chunks are more than 70% of total chunks")
    }

    val transferReport =TransferReport(
      TransferReport.ReportType.MISSING_CHUNKS,
      totalPages.toInt(),
      missedSequenceNumbers.sliceArray(0 until min(defaultTransferReportPageSize, missedCount))
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
    Log.d(logTag, "SequenceNumber: ${Util.twoBytesToIntBigEndian(chunkData.copyOfRange(0,2))},  Sha256: ${Util.getSha256(chunkData)}")
    assembler?.addChunk(chunkData)
  }

  override fun dispatchMessage(msg: Message) {
    try {
      super.dispatchMessage(msg)
    } catch (e: Throwable) {
      transferListener.onException(TransferHandlerException("Exception in Verifier Transfer Handler", e))
      Log.d(logTag, "dispatchMessage " + e.message)
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
