package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.Util.Companion.intToTwoBytesBigEndian

class Chunker(private val data: ByteArray, private val mtuSize: Int = DEFAULT_CHUNK_SIZE) :
  ChunkerBase(mtuSize) {
  private val logTag = "Chunker"
  private var chunksReadCounter: Int = 0
  private val lastChunkByteCount = getLastChunkByteCount(data.size)
  private val totalChunkCount = getTotalChunkCount(data.size)

  init {
    Log.d(logTag, "Total number of chunks: $totalChunkCount")
  }

  fun next(): ByteArray {
    val seqNumber = chunksReadCounter
    chunksReadCounter++

    return chunk(seqNumber)
  }

  fun chunk(seqNumber: Int): ByteArray {
    val fromIndex = seqNumber * effectivePayloadSize

    return if (seqNumber == (totalChunkCount - 1).toInt() && lastChunkByteCount > 0) {
      Log.d(logTag, "fetching last chunk")

      val chunkLength = lastChunkByteCount + chunkMetaSize
      chunk(seqNumber, chunkLength, fromIndex, fromIndex + lastChunkByteCount)
    } else {
      val toIndex = (seqNumber + 1) * effectivePayloadSize
      chunk(seqNumber, mtuSize, fromIndex, toIndex)
    }
  }

  private fun chunk(seqNumber: Int, chunkLength: Int, fromIndex: Int, toIndex: Int): ByteArray {
    Log.d(logTag, "fetching chunk size: ${toIndex - fromIndex}, chunkSequenceNumber(0-indexed): $seqNumber")

    return intToTwoBytesBigEndian(seqNumber) + intToTwoBytesBigEndian(chunkLength) + data.copyOfRange(
      fromIndex,
      toIndex
    )
  }

  fun isComplete(): Boolean {
    val isComplete = chunksReadCounter > (totalChunkCount - 1).toInt()
    if (isComplete) {
      Log.d(
        logTag,
        "isComplete: true, totalChunks: $totalChunkCount , chunkReadCounter(1-indexed): $chunksReadCounter"
      )
    }
    return isComplete
  }

}
