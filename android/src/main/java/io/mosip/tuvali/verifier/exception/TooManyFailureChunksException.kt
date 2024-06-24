package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class TooManyFailureChunksException(s: String, crcFailureCount: Int, totalChunkCount: Int) :
  BLEException(s, null, ErrorCode.TooManyFailureChunksException, crcFailureCount, totalChunkCount)
