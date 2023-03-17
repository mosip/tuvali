package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.ArraySize.TwoBytes
import io.mosip.tuvali.transfer.Util.Companion.intToByteArray
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
    val startTime = System.currentTimeMillis()
    for (idx in 0 until totalChunkCount) {
      preSlicedChunks[idx] = chunk(idx)
    }
    //Log.d(logTag, "Chunks pre-populated in ${System.currentTimeMillis() - startTime} ms time")
  }

  fun next(): ByteArray {
    val seqIndex = chunksReadCounter
    chunksReadCounter++
    return preSlicedChunks[seqIndex]!!
  }

  fun chunkBySequenceNumber(num: Int): ByteArray {
    return preSlicedChunks[num]!!
  }

  private fun chunk(seqIndex: Int): ByteArray {
    val fromIndex = seqIndex * effectivePayloadSize

    return if (seqIndex == (totalChunkCount - 1).toInt() && lastChunkByteCount > 0) {
      Log.d(logTag, "fetching last chunk")
      frameChunk(seqIndex, fromIndex, fromIndex + lastChunkByteCount)
    } else {
      val toIndex = (seqIndex + 1) * effectivePayloadSize
      frameChunk(seqIndex, fromIndex, toIndex)
    }
  }

  /*
  <--------------------------------------------------Max Data Bytes -------------------------------------------------------------->
  +-----------------------+-----------------------------+-------------------------------------------------------------------------+
  |                       |                             |                                                                         |
  |  chunk sequence no    |   checksum value of data    |         chunk payload                                                   |
  |      (2 bytes)        |         (2 bytes)           |       (upto MaxDataBytes -4 bytes)                                      |
  |                       |                             |                                                                         |
  +-----------------------+-----------------------------+-------------------------------------------------------------------------+
   */
  private fun frameChunk(seqIndex: Int, fromIndex: Int, toIndex: Int): ByteArray {
    //Log.d(logTag, "fetching chunk size: ${toIndex - fromIndex}, chunkSequenceNumber(0-indexed): $seqNumber")
    val dataChunk = data.copyOfRange(fromIndex, toIndex)
    val crc = CheckValue.get(dataChunk)
    val seqNumber = seqIndex +1

    return intToByteArray(seqNumber, TwoBytes) + intToByteArray(crc.toInt(), TwoBytes) + dataChunk
  }

  fun isComplete(): Boolean {
    val isComplete = chunksReadCounter > (totalChunkCount - 1).toInt()
    if (isComplete) {
      Log.d(
        logTag,
        "isComplete: true, totalChunks: $totalChunkCount , chunkReadCounter(1-indexed): $chunksReadCounter"
      )
    }
    return isComplete
  }
}
