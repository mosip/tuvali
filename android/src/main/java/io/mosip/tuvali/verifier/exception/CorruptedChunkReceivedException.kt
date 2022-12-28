package io.mosip.tuvali.verifier.exception

class CorruptedChunkReceivedException(size: Int, receivedSeqNumber: Int, receivedMtuSize: Int) : Throwable(
  "size: $size, receivedSeqNumber: $receivedSeqNumber, receivedMtuSize: $receivedMtuSize"
) {}
