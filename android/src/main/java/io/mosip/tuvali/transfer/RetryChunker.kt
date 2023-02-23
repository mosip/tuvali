package io.mosip.tuvali.transfer

import android.util.Log
import io.mosip.tuvali.transfer.Util.Companion.getLogTag

class RetryChunker(private val chunker: Chunker, private val missedSequences: IntArray) {
  private val logTag = getLogTag("RetryChunker")
  private var seqCounter = 0

  init {
    Log.d(logTag, "Total number of missedChunks: ${missedSequences.size}")
  }

  fun next(): ByteArray {
    val missedSeqNo = missedSequences[seqCounter]
    seqCounter++

    return chunker.chunkBySequenceNumber(missedSeqNo)
  }

  fun isComplete(): Boolean {
    return seqCounter == missedSequences.size
  }
}
