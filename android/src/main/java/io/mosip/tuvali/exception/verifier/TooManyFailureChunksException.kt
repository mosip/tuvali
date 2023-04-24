package io.mosip.tuvali.exception.verifier

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class TooManyFailureChunksException(s: String) : BLEException(s, null, ErrorCode.TooManyFailureChunksException)
