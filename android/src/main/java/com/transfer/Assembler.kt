package com.transfer

import com.verifier.exception.CorruptedChunkReceivedException

@OptIn(ExperimentalUnsignedTypes::class)
class Assembler(private val totalSize: Int) {
  private val seqNumberReservedByteSize = 2
  private val mtuReservedByteSize = 2
  private val chunkMetaSize = seqNumberReservedByteSize + mtuReservedByteSize
  private var data: UByteArray = ubyteArrayOf()
  private var chunkCount: Int = 0
  private var lastReadSeqNumber: Int? = null

  init {
    if (totalSize == 0) {
      throw CorruptedChunkReceivedException(0, 0, 0)
    }
  }

  fun addChunk(chunkData: UByteArray) {
    if (chunkData.size < chunkMetaSize) {
      throw CorruptedChunkReceivedException(chunkData.size, 0, 0)
    }
    val seqNumber = twoBytesToInt(chunkData.copyOfRange(0, 2))
    val mtuSize = twoBytesToInt(chunkData.copyOfRange(2, 4))
    if (chunkData.size > mtuSize) {
      throw CorruptedChunkReceivedException(chunkData.size, seqNumber, mtuSize)
    }
    if (data.size + (chunkData.size - chunkMetaSize) > totalSize) {
      throw CorruptedChunkReceivedException(chunkData.size, seqNumber, mtuSize)
    }
    if (lastReadSeqNumber != null && (seqNumber - lastReadSeqNumber!!) == 1) {
      lastReadSeqNumber = seqNumber
    }
    chunkCount++
    data.plus(
      chunkData.copyOfRange(
        chunkMetaSize,
        chunkData.size
      )
    )
  }

  fun isComplete(): Boolean {
    return data.size == totalSize
  }

  private fun twoBytesToInt(num: UByteArray): Int {
    val firstByte = num[0]
    val secondByte = num[1]
    return secondByte.toInt() + (256 * firstByte.toInt())
  }
}
