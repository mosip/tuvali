package com.transfer

import android.util.Log
import kotlin.math.ceil

@OptIn(ExperimentalUnsignedTypes::class)
class Chunker(private val data: UByteArray, mtuSize: Int = 500) {
  private val logTag = "Chunker"
  private val seqNumberReservedByteSize = 2
  private val mtuReservedByteSize = 2
  private val effectiveChunkSize = mtuSize - seqNumberReservedByteSize - mtuReservedByteSize
  private var chunksReadCounter: Int = 0
  private val totalChunks: Double = ceil((data.size.toDouble()/effectiveChunkSize.toDouble()))
  private val lastChunkByteCount = data.size % effectiveChunkSize

  init {
      Log.d(logTag, "Total number of chunks: $totalChunks")
  }
  fun next() : UByteArray {
    val fromIndex = chunksReadCounter * effectiveChunkSize
    if (lastChunkByteCount > 0 && chunksReadCounter == (totalChunks - 1).toInt()) {
      chunksReadCounter++
      Log.d(logTag, "fetching last chunk size: ${lastChunkByteCount}, chunkReadCounter: $chunksReadCounter")
      return data.copyOfRange(fromIndex, fromIndex + lastChunkByteCount)
    }

    val toIndex = (chunksReadCounter + 1) * effectiveChunkSize
    chunksReadCounter++
    Log.d(logTag, "fetching next chunk size: ${toIndex - fromIndex}, chunkReadCounter: $chunksReadCounter")
    return data.copyOfRange(fromIndex, toIndex)
  }

  fun isComplete(): Boolean {
    val isComplete = chunksReadCounter > (totalChunks - 1).toInt()
    if (isComplete) {
      Log.d(logTag, "isComplete: true, totalChunks: $totalChunks , chunkReadCounter: $chunksReadCounter")
    }
    return isComplete
  }
}
