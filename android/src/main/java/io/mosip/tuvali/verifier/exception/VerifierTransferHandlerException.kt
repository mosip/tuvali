package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class VerifierTransferHandlerException(
  message: String,
  cause: Exception,
  crcFailureCount: Int? = null,
  totalChunkCount: Int? = null,
) :
  BLEException(
    message,
    cause,
    ErrorCode.VerifierTransferHandlerException,
    crcFailureCount,
    totalChunkCount
  )
