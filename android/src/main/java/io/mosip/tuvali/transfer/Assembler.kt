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
    val crcReceived = twoBytesToIntBigEndian(chunkData.copyOfRange(2,4)).toUShort()

    //Log.d(logTag, "received add chunk received chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta")

    if (chunkSizeGreaterThanMtuSize(chunkData)) {
      Log.e(logTag, "chunkSizeGreaterThanMtuSize chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta")
      return seqNumberInMeta
    }
    if(crcReceivedIsNotEqualToCrcCalculated(chunkData.copyOfRange(4, chunkData.size), crcReceived)){
      return seqNumberInMeta
    }
    lastReadSeqNumber = seqNumberInMeta
    System.arraycopy(chunkData, chunkMetaSize, data, seqNumberInMeta * effectivePayloadSize, (chunkData.size-chunkMetaSize))
    chunkReceivedMarker[seqNumberInMeta] = chunkReceivedMarkerByte
    Log.d(logTag, "adding chunk complete at index(0-based): ${seqNumberInMeta}, received chunkSize: ${chunkData.size}")
    return seqNumberInMeta
  }

  private fun crcReceivedIsNotEqualToCrcCalculated(
    data: ByteArray,
    crc: UShort
  ) = !CheckValue.verify(data, crc)


  private fun chunkSizeGreaterThanMtuSize(chunkData: ByteArray) = chunkData.size > mtuSize

  fun isComplete(): Boolean {
    if(chunkReceivedMarker.none { it != chunkReceivedMarkerByte }) {
      Log.d(logTag, "Sha256 of complete data received: ${Util.getSha256(data)}")
      return true
    }
    return false
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
