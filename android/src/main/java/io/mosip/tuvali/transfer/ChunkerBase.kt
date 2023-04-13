package io.mosip.tuvali.transfer

import kotlin.math.ceil

typealias ChunkSeqIndex = Int
typealias ChunkSeqNumber = Int

fun ChunkSeqIndex.toSeqNumber(): ChunkSeqNumber {
  return this + 1
}

fun ChunkSeqNumber.toSeqIndex(): ChunkSeqIndex {
  return this - 1
}

open class ChunkerBase(maxDataBytes: Int) {
  private val seqNumberReservedByteSize = 2
  private val crcChecksumValueByteSize = 2
  val chunkMetaSize = seqNumberReservedByteSize + crcChecksumValueByteSize
  val effectivePayloadSize = maxDataBytes - chunkMetaSize

  fun getTotalChunkCount(dataSize: Int): Double {
    return ceil((dataSize.toDouble()/effectivePayloadSize.toDouble()))
  }

  fun getLastChunkByteCount(dataSize: Int): Int {
    return dataSize % effectivePayloadSize
  }
}
