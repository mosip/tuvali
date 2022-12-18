package com.verifier.transfer

import android.util.Log
import com.ble.peripheral.Peripheral
import com.verifier.GattService
import com.verifier.exception.WriteToRemoteException
import java.util.UUID

// Non Thread safe instance
// This is not used, this is to demonstrate complexity of code without state machine
@OptIn(ExperimentalUnsignedTypes::class)
class ReadWriter(private val peripheral: Peripheral) {
  private val logTag = "ReadWriter"
  private lateinit var onTransferCompleteCallback: (UByteArray) -> Unit
  private lateinit var onTransferFailedCallback: (TransferStates, String) -> Unit
  private var isChunkInProcess: Boolean = false
  private var chunker: Chunker? = null
  private var assembler: Assembler? = null

  // Retaining the order of below states are important
  enum class TransferStates {
    UnInitialised,
    RequestSizeWritePending,
    RequestSizeWriteFailed,
    RequestWritePending,
    RequestWriteFailed,
    ResponseSizeReadPending,
    ResponseSizeReadFailed,
    ResponseReadPending,
    ResponseReadFailed,
    TransferComplete
  }

  private var currentState: TransferStates = TransferStates.UnInitialised

  fun sendRequest(
    request: UByteArray,
    onTransferComplete: (response: UByteArray) -> Unit,
    onTransferFailed: (currentState: TransferStates, String) -> Unit
  ) {
    if (currentState != TransferStates.UnInitialised) {
      throw WriteToRemoteException("data transfer already in progress")
    }
    onTransferCompleteCallback = onTransferComplete
    onTransferFailedCallback = onTransferFailed
    chunker = Chunker(request)
    process(request)
  }

  fun onReceivedWriteFromRemote(uuid: UUID, data: UByteArray) {
    if (uuid == GattService.RESPONSE_SIZE_CHAR_UUID && currentState == TransferStates.ResponseSizeReadPending && data.size == 2) {
      val responseSize = data.toString().toInt()
      assembler = Assembler(responseSize)
      isChunkInProcess = true
      currentState = TransferStates.ResponseReadPending
    } else if (uuid == GattService.RESPONSE_CHAR_UUID && currentState == TransferStates.ResponseReadPending && isChunkInProcess) {
      assembleChunk(data)
    }
  }

  private fun assembleChunk(data: UByteArray) {
    assembler?.addChunk(data)
    if (assembler?.isComplete() == true) {
      isChunkInProcess = false
      //TODO: transfer should be complete here
      currentState = TransferStates.TransferComplete
    }
  }

  fun onReadByRemote(uuid: UUID, isRead: Boolean) {
    if (isRequestSizeWriteToRemoteFailed(uuid, isRead)) {
      Log.d(logTag, "onReadByRemote: request size write to remote failed")
      currentState = TransferStates.RequestSizeWriteFailed
    } else if (isRequestChunkWriteToRemoteFailed(uuid, isRead)) {
      Log.d(logTag, "onReadByRemote: request chunk write to remote failed")
      currentState = TransferStates.RequestWriteFailed
    }

    if (isRequestSizeWriteToRemoteSuccessful(uuid, isRead)) {
      Log.d(logTag, "onReadByRemote: request size write to remote successful")
      currentState = TransferStates.RequestWritePending
      isChunkInProcess = true
      writeChunk()
    } else if (isRequestChunkWriteToRemoteSuccessful(uuid, isRead)) {
      writeChunk()
      Log.d(logTag, "onReadByRemote: request chunk write to remote successful")
    } else if (isRequestWriteToRemoteComplete(uuid, isRead)) {
      Log.d(logTag, "onReadByRemote: request write to remote complete")
      currentState = TransferStates.ResponseSizeReadPending
    }
  }

  private fun writeChunk() {
    val chunkBytes = chunker?.next()
    if (chunker?.isComplete() == true) {
      isChunkInProcess = false
    }
    if (chunkBytes != null) {
      peripheral.sendData(GattService.REQUEST_CHAR_UUID, chunkBytes)
    }
  }

  private fun isRequestWriteToRemoteComplete(uuid: UUID, isRead: Boolean) =
    uuid == GattService.REQUEST_CHAR_UUID && currentState == TransferStates.RequestWritePending && !isChunkInProcess && isRead

  private fun isRequestChunkWriteToRemoteSuccessful(uuid: UUID, isRead: Boolean) =
    uuid == GattService.REQUEST_CHAR_UUID && currentState == TransferStates.RequestWritePending && isChunkInProcess && isRead

  private fun isRequestChunkWriteToRemoteFailed(uuid: UUID, isRead: Boolean) =
    uuid == GattService.REQUEST_CHAR_UUID && currentState == TransferStates.RequestWritePending && isChunkInProcess && !isRead

  private fun isRequestSizeWriteToRemoteSuccessful(uuid: UUID, isRead: Boolean) =
    uuid == GattService.REQUEST_SIZE_CHAR_UUID && currentState == TransferStates.RequestSizeWritePending && isRead

  private fun isRequestSizeWriteToRemoteFailed(uuid: UUID, isRead: Boolean) =
    uuid == GattService.REQUEST_SIZE_CHAR_UUID && currentState == TransferStates.RequestSizeWritePending && !isRead

  private fun process(data: UByteArray) {
    currentState = TransferStates.RequestSizeWritePending
    peripheral.sendData(
      GattService.REQUEST_SIZE_CHAR_UUID,
      arrayOf(data.size.toUByte()).toUByteArray()
    )
  }
}
