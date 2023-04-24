package io.mosip.tuvali.ble.central.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class CentralStateHandlerException(message: String, cause: Exception): BLEException(message, cause,
    ErrorCode.CentralStateHandlerException
)
