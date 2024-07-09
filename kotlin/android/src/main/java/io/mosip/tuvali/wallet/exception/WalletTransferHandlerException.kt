package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class WalletTransferHandlerException(message: String, cause: Exception): BLEException(message, cause, ErrorCode.WalletTransferHandlerException)
