package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.ByteCount.TwoBytes
import io.mosip.tuvali.transfer.Util.Companion.intToNetworkOrderedByteArray
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

class Chunker(private val data: ByteArray, private val maxDataBytes: Int) :
  ChunkerBase(maxDataBytes) {
  private val logTag = getLogTag(javaClass.simpleName)
  private var chunksReadCounter: Int = 0
  private val lastChunkByteCount = getLastChunkByteCount(data.size)
  private val totalChunkCount = getTotalChunkCount(data.size).toInt()
  private val preSlicedChunks: Array<ByteArray?> = Array(totalChunkCount) { null }

  init {
    Log.i(logTag, "Total number of chunks calculated: $totalChunkCount")
//    val startTime = System.currentTimeMillis()
    for (idx in 0 until totalChunkCount) {
      preSlicedChunks[idx] = chunk(idx)
    }
    //Log.d(logTag, "Chunks pre-populated in ${System.currentTimeMillis() - startTime} ms time")
  }

  fun next(): ByteArray {
    return preSlicedChunks[chunksReadCounter++]!!
  }

  fun chunkBySequenceNumber(missedSeqNumber: ChunkSeqNumber): ByteArray {
    return preSlicedChunks[missedSeqNumber.toSeqIndex()]!!
  }

  private fun chunk(seqIndex: ChunkSeqIndex): ByteArray {
    val fromIndex = seqIndex * effectivePayloadSize
    return if (isLastChunkSmallerSize(seqIndex)) {
      frameChunk(seqIndex.toSeqNumber(), fromIndex, fromIndex + lastChunkByteCount)
    } else {
      val toIndex = fromIndex + effectivePayloadSize
      frameChunk(seqIndex.toSeqNumber(), fromIndex, toIndex)
    }
  }

  private fun isLastChunkSmallerSize(seqIndex: Int) =
    isLastChunkIndex(seqIndex) && lastChunkByteCount > 0

  private fun isLastChunkIndex(seqIndex: Int) = seqIndex == (totalChunkCount - 1)

  /*
  <--------------------------------------------------Max Data Bytes -------------------------------------------------------------->
  +-----------------------+-----------------------------+-------------------------------------------------------------------------+
  |                       |                             |                                                                         |
  |  chunk sequence no    |   checksum value of data    |         chunk payload                                                   |
  |      (2 bytes)        |         (2 bytes)           |       (upto MaxDataBytes -4 bytes)                                      |
  |                       |                             |                                                                         |
  +-----------------------+-----------------------------+-------------------------------------------------------------------------+
   */
  private fun frameChunk(seqNumber: Int, fromIndex: Int, toIndex: Int): ByteArray {
    //Log.d(logTag, "fetching chunk size: ${toIndex - fromIndex}, chunkSequenceNumber(0-indexed): $seqNumber")
    val dataChunk = data.copyOfRange(fromIndex, toIndex)
    val crc = CheckValue.get(dataChunk)


    return intToNetworkOrderedByteArray(seqNumber, TwoBytes) + intToNetworkOrderedByteArray(crc.toInt(), TwoBytes) + dataChunk

  }

  fun isComplete(): Boolean {
    Log.i(logTag,"chunksReadCounter: $chunksReadCounter")
    val isComplete = chunksReadCounter >= totalChunkCount
    if (isComplete) {
      Log.d(
        logTag,
        "isComplete: true, totalChunks: $totalChunkCount , chunkReadCounter(1-indexed): $chunksReadCounter"
      )
    }
    return isComplete
  }
}
