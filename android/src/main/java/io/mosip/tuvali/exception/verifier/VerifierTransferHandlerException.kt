package io.mosip.tuvali.exception.verifier

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class VerifierTransferHandlerException(message: String, cause: Exception): BLEException(message, cause, ErrorCode.VerifierTransferHandlerException)
