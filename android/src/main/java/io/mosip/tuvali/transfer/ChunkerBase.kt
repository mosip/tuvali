package io.mosip.tuvali.transfer

import kotlin.math.ceil

const val DEFAULT_CHUNK_SIZE = 182

open class ChunkerBase(maxDataBytes: Int = DEFAULT_CHUNK_SIZE) {
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
