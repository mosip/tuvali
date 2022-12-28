package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.Util.Companion.intToTwoBytesBigEndian
import kotlin.math.ceil

class Chunker(private val data: ByteArray, private val mtuSize: Int = 500) {
  private val logTag = "Chunker"
  private val seqNumberReservedByteSize = 2
  private val mtuReservedByteSize = 2
  private val chunkMetaSize = seqNumberReservedByteSize + mtuReservedByteSize
  private val effectiveChunkSize = mtuSize - chunkMetaSize
  private var chunksReadCounter: Int = 0
  private val totalChunks: Double = ceil((data.size.toDouble()/effectiveChunkSize.toDouble()))
  private val lastChunkByteCount = data.size % effectiveChunkSize

  init {
      Log.d(logTag, "Total number of chunks: $totalChunks")
  }

  fun next() : ByteArray {
    val fromIndex = chunksReadCounter * effectiveChunkSize
    if (lastChunkByteCount > 0 && chunksReadCounter == (totalChunks - 1).toInt()) {
      val seqNumber = chunksReadCounter
      val chunkLength = lastChunkByteCount + chunkMetaSize
      chunksReadCounter++
      Log.d(logTag, "fetching last chunk size: ${lastChunkByteCount}, chunkSequenceNumber(0-indexed): $seqNumber")
      return intToTwoBytesBigEndian(seqNumber) + intToTwoBytesBigEndian(chunkLength) + data.copyOfRange(fromIndex, fromIndex + lastChunkByteCount)
    }

    val toIndex = (chunksReadCounter + 1) * effectiveChunkSize
    val seqNumber = chunksReadCounter
    chunksReadCounter++
    Log.d(logTag, "fetching chunk size: ${toIndex - fromIndex}, chunkSequenceNumber(0-indexed): $seqNumber")
    return intToTwoBytesBigEndian(seqNumber) + intToTwoBytesBigEndian(mtuSize) + data.copyOfRange(fromIndex, toIndex)
  }

  fun isComplete(): Boolean {
    val isComplete = chunksReadCounter > (totalChunks - 1).toInt()
    if (isComplete) {
      Log.d(logTag, "isComplete: true, totalChunks: $totalChunks , chunkReadCounter(1-indexed): $chunksReadCounter")
    }
    return isComplete
  }
}
