package io.mosip.tuvali.exception.ble

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class CentralStateHandlerException(message: String, cause: Exception): BLEException(message, cause,
  ErrorCode.CentralStateHandlerException
)
