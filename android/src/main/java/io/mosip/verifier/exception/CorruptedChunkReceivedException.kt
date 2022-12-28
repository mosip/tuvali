package io.mosip.verifier.exception

class CorruptedChunkReceivedException(size: Int, receivedSeqNumber: Int, receivedMtuSize: Int) : Throwable(
  "size: $size, receivedSeqNumber: $receivedSeqNumber, receivedMtuSize: $receivedMtuSize"
) {}
