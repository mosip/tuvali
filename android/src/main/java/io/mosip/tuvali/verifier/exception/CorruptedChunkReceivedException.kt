package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode

class CorruptedChunkReceivedException(size: Int, receivedSeqNumber: Int, receivedMtuSize: Int) : VerifierException(
  "size: $size, receivedSeqNumber: $receivedSeqNumber, receivedMtuSize: $receivedMtuSize",
  ErrorCode.CorruptedChunkReceivedException
)
