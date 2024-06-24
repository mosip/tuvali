package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

open class VerifierException(
  message: String,
  cause: Exception? = null,
  crcFailureCount: Int? = null,
  totalChunkCount: Int? = null
) :
  BLEException(
    message,
    cause,
    errorCode = ErrorCode.VerifierUnknownException,
    crcFailureCount = crcFailureCount,
    totalChunkCount = totalChunkCount
  )
