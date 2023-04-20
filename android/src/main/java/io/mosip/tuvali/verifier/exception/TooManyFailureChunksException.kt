package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class TooManyFailureChunksException(s: String) : BLEException(s, null, ErrorCode.TooManyFailureChunksException)
