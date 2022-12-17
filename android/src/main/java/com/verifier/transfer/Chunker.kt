package com.verifier.transfer

import kotlin.math.ceil

class Chunker(private val data: UByteArray) {
  private val mtuSize= 200
  private val seqNumberReservedByteSize = 2
  private val mtuReservedByteSize = 2
  private val effectiveChunkSize = mtuSize - seqNumberReservedByteSize - mtuReservedByteSize
  private var chunksReadCounter: Int = 0
  private val totalChunks: Double = ceil((data.size/effectiveChunkSize).toDouble())
  private val lastChunkByteCount = data.size % effectiveChunkSize

  fun next() : UByteArray {
    val fromIndex = chunksReadCounter * effectiveChunkSize
    if (lastChunkByteCount > 0 && chunksReadCounter == (totalChunks - 2).toInt()) {
      chunksReadCounter++
      return data.copyOfRange(fromIndex, fromIndex + lastChunkByteCount)
    }

    val toIndex = (chunksReadCounter + 1) * effectiveChunkSize
    chunksReadCounter++
    return data.copyOfRange(fromIndex, toIndex)
  }

  fun isComplete(): Boolean {
    return chunksReadCounter >= (totalChunks-1).toInt()
  }
}
