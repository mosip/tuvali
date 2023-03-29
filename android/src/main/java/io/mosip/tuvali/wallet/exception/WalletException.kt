package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode

open class WalletException(message: String, errorCode: ErrorCode): BLEException(message, null, errorCode = errorCode)
