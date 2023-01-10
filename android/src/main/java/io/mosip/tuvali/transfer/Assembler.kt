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
      return 0
    }
    val seqNumberInMeta = twoBytesToIntBigEndian(chunkData.copyOfRange(0, 2))
    val chunkSizeInMeta = twoBytesToIntBigEndian(chunkData.copyOfRange(2, 4))
    val expectedPayloadSizeInChunk = chunkSizeInMeta - chunkMetaSize
    Log.d(logTag, "received add chunk received chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta, expectedPayloadSizeInChunk: $expectedPayloadSizeInChunk")
    if (payloadSizeDidNotMatchTheSizeInMeta(chunkData, chunkSizeInMeta)) {
      Log.e(logTag, "payloadSizeDidNotMatchTheSizeInMeta chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta, expectedPayloadSizeInChunk: $expectedPayloadSizeInChunk, chunkSizeInMeta: $chunkSizeInMeta")
      return seqNumberInMeta
    }
    if (chunkSizeGreaterThanMtuSize(chunkData)) {
      Log.e(logTag, "chunkSizeGreaterThanMtuSize chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta, expectedPayloadSizeInChunk: $expectedPayloadSizeInChunk")
      return seqNumberInMeta
    }
    if (addingLastChunkShouldNotBeMoreThanExpectedSize(seqNumberInMeta, expectedPayloadSizeInChunk)) {
      Log.e(logTag, "addingLastChunkShouldNotBeMoreThanExpectedSize chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta, expectedPayloadSizeInChunk: $expectedPayloadSizeInChunk")
      return seqNumberInMeta
    }
    lastReadSeqNumber = seqNumberInMeta
    System.arraycopy(chunkData, chunkMetaSize, data, seqNumberInMeta * effectivePayloadSize, expectedPayloadSizeInChunk)
    chunkReceivedMarker[seqNumberInMeta] = chunkReceivedMarkerByte
    Log.d(logTag, "adding chunk complete at index(0-based): ${seqNumberInMeta}, received chunkSize: ${chunkData.size}")
    return seqNumberInMeta
  }

  private fun payloadSizeDidNotMatchTheSizeInMeta(
      chunkData: ByteArray,
      payloadSizeInChunk: Int
  ) = chunkData.size != payloadSizeInChunk

  private fun chunkSizeGreaterThanMtuSize(chunkData: ByteArray) = chunkData.size > mtuSize

  private fun addingLastChunkShouldNotBeMoreThanExpectedSize(
      seqNumber: Int,
      payloadSizeInChunk: Int
  ) = (seqNumber * effectivePayloadSize) + payloadSizeInChunk > totalSize

  fun isComplete(): Boolean {
    return chunkReceivedMarker.none { it != chunkReceivedMarkerByte }
  }

  fun getMissedSequenceNumbers(): IntArray {
    var missedSeqNumbers = intArrayOf()
    chunkReceivedMarker.forEachIndexed() { i, elem ->
      if (elem != chunkReceivedMarkerByte) {
        Log.d(logTag, "getMissedSequenceNumbers: adding missed sequence number $i")
        missedSeqNumbers += i
      }
    }
    return missedSeqNumbers
  }

  fun data(): ByteArray {
    return data
  }
}
