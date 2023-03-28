package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.BLEException

open class VerifierException(message: String, errorCode: Int): BLEException(message, null, errorCode = errorCode)
