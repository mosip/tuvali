package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class VerifierTransferHandlerException(message: String, cause: Throwable): BLEException(message, cause, ErrorCode.VerifierTransferHandlerException)
