package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class WalletStateHandlerException(message: String, cause: Exception): BLEException(message, cause, ErrorCode.WalletStateHandlerException)
