package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class VerifierStateHandlerException(message: String, cause: Exception): BLEException(message, cause, ErrorCode.VerifierStateHandlerException)
