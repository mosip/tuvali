package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.Util.Companion.twoBytesToIntBigEndian
import io.mosip.tuvali.verifier.exception.CorruptedChunkReceivedException

class Assembler(private val totalSize: Int, private val mtuSize: Int = DEFAULT_CHUNK_SIZE): ChunkerBase(mtuSize) {
  private val logTag = "Assembler"
  private var data: ByteArray = ByteArray(totalSize)
  private var lastReadSeqNumber: Int? = null
  private val totalChunkCount = getTotalChunkCount(totalSize)
  private var chunkReceivedMarker = ByteArray(totalChunkCount.toInt())
  private val chunkReceivedMarkerByte: Byte = 1

  init {
    Log.d(logTag, "expected total chunk size: $totalSize")
    if (totalSize == 0) {
      throw CorruptedChunkReceivedException(0, 0, 0)
    }
  }

  fun addChunk(chunkData: ByteArray): Int {
    if (chunkData.size < chunkMetaSize) {
      Log.e(logTag, "received invalid chunk chunkSize: ${chunkData.size}, lastReadSeqNumber: $lastReadSeqNumber")
      throw CorruptedChunkReceivedException(chunkData.size, 0, 0)
    }
    val seqNumber = twoBytesToIntBigEndian(chunkData.copyOfRange(0, 2))
    val payloadSizeInChunk = twoBytesToIntBigEndian(chunkData.copyOfRange(2, 4)) - chunkMetaSize
    Log.d(logTag, "received add chunk received chunkSize: ${chunkData.size}, seqNumber: $seqNumber, payloadSizeInChunk: $payloadSizeInChunk")
    if (chunkData.size > mtuSize) {
      throw CorruptedChunkReceivedException(chunkData.size, seqNumber, mtuSize)
    }
    if ((seqNumber * effectivePayloadSize) + payloadSizeInChunk > totalSize) {
      throw CorruptedChunkReceivedException(chunkData.size, seqNumber, payloadSizeInChunk)
    }
    lastReadSeqNumber = seqNumber
    System.arraycopy(chunkData, chunkMetaSize, data, seqNumber * effectivePayloadSize, payloadSizeInChunk)
    chunkReceivedMarker[seqNumber] = chunkReceivedMarkerByte
    Log.d(logTag, "adding chunk complete at index(0-based): ${seqNumber}, received chunkSize: ${chunkData.size}")
    return seqNumber
  }

  fun isComplete(): Boolean {
    return chunkReceivedMarker.none { it != chunkReceivedMarkerByte }
  }

  fun getMissedSequenceNumbers(): IntArray {
    val missedSeqNumbers = intArrayOf()
    chunkReceivedMarker.forEachIndexed() { i, elem ->
      if (elem != chunkReceivedMarkerByte) {
        missedSeqNumbers + i
      }
    }
    return missedSeqNumbers
  }

  fun data(): ByteArray {
    return data
  }
}
