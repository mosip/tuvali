package io.mosip.tuvali.exception.verifier

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

open class VerifierException(message: String,  cause: Exception? = null): BLEException(message, cause, errorCode = ErrorCode.VerifierUnknownException)
