package io.mosip.tuvali.transfer

import android.util.Log

class RetryChunker(private val chunker: Chunker, private val missedSequences: IntArray) {
  private val logTag = "RetryChunker"
  private var seqCounter = 0;

  init {
    Log.d(logTag, "Total number of missedChunks: ${missedSequences.size}")
  }

  fun next(): ByteArray {
    val missedSeqNo = missedSequences[seqCounter]
    seqCounter++;

    return chunker.chunkBySequenceNumber(missedSeqNo)
  }

  fun isComplete(): Boolean {
    return seqCounter == missedSequences.size - 1
  }
}
