package io.mosip.tuvali.transfer

import kotlin.math.ceil

const val DEFAULT_CHUNK_SIZE = 182

open class ChunkerBase(mtuSize: Int = DEFAULT_CHUNK_SIZE) {
  private val seqNumberReservedByteSize = 2
  private val mtuReservedByteSize = 2
  val chunkMetaSize = seqNumberReservedByteSize + mtuReservedByteSize
  val effectivePayloadSize = mtuSize - chunkMetaSize

  fun getTotalChunkCount(dataSize: Int): Double {
    return ceil((dataSize.toDouble()/effectivePayloadSize.toDouble()))
  }

  fun getLastChunkByteCount(dataSize: Int): Int {
    return dataSize % effectivePayloadSize
  }
}
