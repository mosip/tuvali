package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode

class VerifierStateHandlerException(message: String, cause: Throwable): BLEException(message, cause, ErrorCode.VerifierStateHandlerException)
