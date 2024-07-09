package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class ServiceNotFoundException(s: String) : BLEException(s, null, ErrorCode.ServiceNotFoundException)
