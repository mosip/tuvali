package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

open class VerifierException(message: String,  cause: Throwable? = null): BLEException(message, cause, errorCode = ErrorCode.VerifierUnknownException)
