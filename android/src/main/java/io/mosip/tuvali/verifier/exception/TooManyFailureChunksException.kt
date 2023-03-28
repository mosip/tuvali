package io.mosip.tuvali.verifier.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCodes

class TooManyFailureChunksException(s: String) : VerifierException(s, ErrorCodes.TooManyFailureChunksException.code)
