package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode

class TooManyFailureChunksException(s: String) : VerifierException(s, ErrorCode.TooManyFailureChunksException)
