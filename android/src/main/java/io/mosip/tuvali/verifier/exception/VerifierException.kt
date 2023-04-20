package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode

open class VerifierException(message: String,  cause: Throwable? = null): BLEException(message, cause, errorCode = ErrorCode.VerifierUnknownException)
