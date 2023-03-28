package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCodes

class CorruptedChunkReceivedException(size: Int, receivedSeqNumber: Int, receivedMtuSize: Int) : VerifierException(
  "size: $size, receivedSeqNumber: $receivedSeqNumber, receivedMtuSize: $receivedMtuSize",
  ErrorCodes.CorruptedChunkReceivedException.code
) {}
