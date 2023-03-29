package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode

class UnsupportedMTUSizeException(s: String) : VerifierException(s, ErrorCode.UnsupportedMTUSizeException) {}
