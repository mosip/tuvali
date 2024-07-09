package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class UnsupportedMTUSizeException(s: String) : BLEException(s, null, ErrorCode.UnsupportedMTUSizeException) {}
