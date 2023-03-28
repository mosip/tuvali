package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCodes

class UnsupportedMTUSizeException(s: String) : VerifierException(s, ErrorCodes.UnsupportedMTUSizeException.code) {}
