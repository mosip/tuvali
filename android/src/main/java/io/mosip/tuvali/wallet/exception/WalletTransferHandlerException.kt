package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class WalletTransferHandlerException(message: String, cause: Throwable): BLEException(message, cause, ErrorCode.WalletTransferHandlerException)
