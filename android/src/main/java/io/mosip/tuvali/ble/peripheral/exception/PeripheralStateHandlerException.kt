package io.mosip.tuvali.ble.peripheral.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class PeripheralStateHandlerException(message: String, cause: Exception): BLEException(message, cause,
    ErrorCode.PeripheralStateHandlerException
)
