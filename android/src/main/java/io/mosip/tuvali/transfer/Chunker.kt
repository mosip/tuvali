package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.Util.Companion.intToTwoBytesBigEndian
import kotlin.math.ceil

class Chunker(private val data: ByteArray, private val mtuSize: Int = DEFAULT_CHUNK_SIZE): ChunkerBase(mtuSize) {
  private val logTag = "Chunker"
  private var chunksReadCounter: Int = 0
  private val lastChunkByteCount = getLastChunkByteCount(data.size)
  private val totalChunkCount = getTotalChunkCount(data.size)

  init {
      Log.d(logTag, "Total number of chunks: ${totalChunkCount}")
  }

  fun next() : ByteArray {
    val fromIndex = chunksReadCounter * effectivePayloadSize
    if (lastChunkByteCount > 0 && chunksReadCounter == (totalChunkCount - 1).toInt()) {
      val seqNumber = chunksReadCounter
      val chunkLength = lastChunkByteCount + chunkMetaSize
      chunksReadCounter++
      Log.d(logTag, "fetching last chunk size: $lastChunkByteCount, chunkSequenceNumber(0-indexed): $seqNumber")
      return intToTwoBytesBigEndian(seqNumber) + intToTwoBytesBigEndian(chunkLength) + data.copyOfRange(fromIndex, fromIndex + lastChunkByteCount)
    }

    val toIndex = (chunksReadCounter + 1) * effectivePayloadSize
    val seqNumber = chunksReadCounter
    chunksReadCounter++
    Log.d(logTag, "fetching chunk size: ${toIndex - fromIndex}, chunkSequenceNumber(0-indexed): $seqNumber")
    return intToTwoBytesBigEndian(seqNumber) + intToTwoBytesBigEndian(mtuSize) + data.copyOfRange(fromIndex, toIndex)
  }

  fun isComplete(): Boolean {
    val isComplete = chunksReadCounter > (totalChunkCount - 1).toInt()
    if (isComplete) {
      Log.d(logTag, "isComplete: true, totalChunks: $totalChunkCount , chunkReadCounter(1-indexed): $chunksReadCounter")
    }
    return isComplete
  }
}
