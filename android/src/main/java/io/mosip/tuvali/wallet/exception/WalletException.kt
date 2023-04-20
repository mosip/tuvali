package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

open class WalletException(message: String, cause: Throwable?): BLEException(message, cause, errorCode = ErrorCode.WalletUnknownException)
