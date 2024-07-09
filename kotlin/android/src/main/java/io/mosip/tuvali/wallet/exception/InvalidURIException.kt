package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class InvalidURIException(message: String): BLEException(message, null, ErrorCode.InvalidURIException)
