package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.ByteCount.TwoBytes
import io.mosip.tuvali.transfer.Util.Companion.networkOrderedByteArrayToInt
import io.mosip.tuvali.verifier.exception.CorruptedChunkReceivedException
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

class Assembler(totalSize: Int, private val maxDataBytes: Int ): ChunkerBase(maxDataBytes) {
  private val logTag = getLogTag(javaClass.simpleName)
  private var data: ByteArray = ByteArray(totalSize)
  private var lastReadSeqNumber: Int? = null
  val totalChunkCount = getTotalChunkCount(totalSize)
  private var chunkReceivedMarker = ByteArray(totalChunkCount.toInt())
  private val chunkReceivedMarkerByte: Byte = 1

  init {
    Log.i(logTag, "expected total chunk size: $totalSize")
    if (totalSize == 0) {
      //Todo: The exception name and the parameter doesn't makes sense
      throw CorruptedChunkReceivedException(0, 0, 0)
    }
  }

  fun addChunk(chunkData: ByteArray): Int {
    if (chunkData.size < chunkMetaSize) {
      Log.e(logTag, "received invalid chunk chunkSize: ${chunkData.size}, lastReadSeqNumber: $lastReadSeqNumber")
      return 0
    }

    val seqNumberInMeta: ChunkSeqNumber = networkOrderedByteArrayToInt(chunkData.copyOfRange(0, 2), TwoBytes)
    val crcReceived = networkOrderedByteArrayToInt(chunkData.copyOfRange(2,4), TwoBytes).toUShort()

    //Log.d(logTag, "received add chunk received chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta")

    if (chunkSizeGreaterThanMaxDataBytes(chunkData)) {
      Log.e(logTag, "chunkSizeGreaterThanMaxDataBytes chunkSize: ${chunkData.size}, seqNumberInMeta: $seqNumberInMeta")
      return seqNumberInMeta
    }
    if(crcReceivedIsNotEqualToCrcCalculated(chunkData.copyOfRange(4, chunkData.size), crcReceived)){
      return seqNumberInMeta
    }
    lastReadSeqNumber = seqNumberInMeta
    val seqIndex = seqNumberInMeta.toSeqIndex()
    System.arraycopy(chunkData, chunkMetaSize, data, seqIndex * effectivePayloadSize, (chunkData.size-chunkMetaSize))
    chunkReceivedMarker[seqIndex] = chunkReceivedMarkerByte
    //Log.d(logTag, "adding chunk complete at index(0-based): ${seqNumberInMeta}, received chunkSize: ${chunkData.size}")
    return seqNumberInMeta
  }

  private fun crcReceivedIsNotEqualToCrcCalculated(
    data: ByteArray,
    crc: UShort
  ) = !CheckValue.verify(data, crc)


  private fun chunkSizeGreaterThanMaxDataBytes(chunkData: ByteArray) = chunkData.size > maxDataBytes

  fun isComplete(): Boolean {
    if(chunkReceivedMarker.none { it != chunkReceivedMarkerByte }) {
      //Log.i(logTag, "Sha256 of complete data received: ${Util.getSha256(data)}")
      return true
    }
    return false
  }

  fun getMissedSequenceNumbers(): IntArray {
    var missedSeqNumberList = intArrayOf()
    chunkReceivedMarker.forEachIndexed() { missedChunkSeqIndex: ChunkSeqIndex, elem ->
      if (elem != chunkReceivedMarkerByte) {
        //Log.d(logTag, "getMissedSequenceNumbers: adding missed sequence number $missedChunkSeqIndex")
        missedSeqNumberList += missedChunkSeqIndex.toSeqNumber()
      }
    }
    return missedSeqNumberList
  }

  fun data(): ByteArray {
    return data
  }
}
