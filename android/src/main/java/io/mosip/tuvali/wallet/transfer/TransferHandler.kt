package io.mosip.tuvali.wallet.transfer

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import io.mosip.tuvali.ble.central.Central
import io.mosip.tuvali.transfer.Chunker
import io.mosip.tuvali.transfer.RetryChunker
import io.mosip.tuvali.transfer.TransmissionReport
import io.mosip.tuvali.verifier.GattService
import io.mosip.tuvali.verifier.transfer.message.ResponseTransferFailedMessage
import io.mosip.tuvali.wallet.transfer.message.*
import java.util.*

class TransferHandler(looper: Looper, private val central: Central, val serviceUUID: UUID, private val transferListener: ITransferListener) :
  Handler(looper) {
  private lateinit var retryChunker: RetryChunker
  private val logTag = "TransferHandler"
  private var chunkCounter = 0;
  private var isRetryFrame = false;

  enum class States {
    UnInitialised,
    ResponseSizeWritePending,
    ResponseSizeWriteSuccess,
    ResponseSizeWriteFailed,
    ResponseWritePending,
    ResponseWriteFailed,
    TransferComplete,
    WaitingForTransferReport,
    HandlingTransferReport,
    TransferVerified,
    PartiallyTransferred,
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
      IMessage.TransferMessageTypes.READ_TRANSMISSION_REPORT.ordinal -> {
        currentState = States.WaitingForTransferReport
        requestTransmissionReport()
      }
      IMessage.TransferMessageTypes.HANDLE_TRANSMISSION_REPORT.ordinal -> {
        currentState = States.HandlingTransferReport
        val handleTransmissionReportMessage = msg.obj as HandleTransmissionReportMessage
        handleTransmissionReport(handleTransmissionReportMessage.report)
      }
      IMessage.TransferMessageTypes.RESPONSE_CHUNK_WRITE_SUCCESS.ordinal -> {
        if(isRetryFrame) {
          sendRetryResponseChunk()
        } else {
          sendResponseChunk()
          chunkCounter++
        }
      }
      IMessage.TransferMessageTypes.RESPONSE_CHUNK_WRITE_FAILURE.ordinal -> {
        val responseChunkWriteFailureMessage = msg.obj as ResponseChunkWriteFailureMessage
        this.sendMessage(ResponseTransferFailureMessage("chunk write failed with err: ${responseChunkWriteFailureMessage.err}"))
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE.ordinal -> {
        // TODO: Let higher level know
        Log.d(logTag, "handleMessage: Successfully transferred vc in ${System.currentTimeMillis() - responseStartTimeInMillis}ms")
        currentState = States.TransferComplete
        this.sendMessage(ReadTransmissionReportMessage())
      }
      IMessage.TransferMessageTypes.RESPONSE_TRANSFER_FAILED.ordinal -> {
        val responseTransferFailedMessage = msg.obj as ResponseTransferFailedMessage
        Log.d(logTag, "handleMessage: response transfer failed")
        transferListener.onResponseSendFailure(responseTransferFailedMessage.errorMsg)
        currentState = States.ResponseSizeWriteFailed
      }
      IMessage.TransferMessageTypes.INIT_RETRY_TRANSFER.ordinal -> {
        val initRetryTransferMessage = msg.obj as InitRetryTransferMessage
        isRetryFrame = true
        retryChunker = RetryChunker(chunker!!, initRetryTransferMessage.missedSequences)
        sendRetryResponseChunk()
      }
    }
  }

  private fun sendRetryResponseChunk() {
    if (retryChunker.isComplete()) {
      this.sendMessage(ReadTransmissionReportMessage())
    } else {
      writeResponseChunk(retryChunker.next())
    }
  }

  private fun handleTransmissionReport(report: TransmissionReport) {
    if (report.type == TransmissionReport.ReportType.SUCCESS) {
      currentState = States.TransferVerified
      transferListener.onResponseSent()
    } else if(report.type == TransmissionReport.ReportType.MISSING_CHUNKS && report.missingSequences != null && !isRetryFrame) {
      currentState = States.PartiallyTransferred
      this.sendMessage(InitRetryTransferMessage(report.missingSequences))
    } else {
      this.sendMessage(ResponseTransferFailureMessage("Invalid Report"))
    }
  }

  private fun requestTransmissionReport() {
    central.write(serviceUUID, GattService.SEMAPHORE_CHAR_UUID, ByteArray(0))
  }

  private fun sendResponseChunk() {
    Log.d(logTag, "Writing Chunk: $chunkCounter and is complete: ${chunker?.isComplete()}")
    if (chunker?.isComplete() == true) {
      this.sendMessage(ResponseTransferCompleteMessage())
      return
    }

    val chunkArray = chunker?.next()
    if (chunkArray != null) {
      writeResponseChunk(chunkArray)
    }
  }

  private fun writeResponseChunk(chunkArray: ByteArray) {
    central.write(
      serviceUUID,
      GattService.RESPONSE_CHAR_UUID,
      chunkArray
    )
  }

  fun readSemaphoreAckDelayed() {
    this.sendMessageDelayed(ReadTransmissionReportMessage(), 5)
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
