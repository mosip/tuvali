package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.exception.BLEException

open class WalletException(message: String, errorCode: Int): BLEException(message, null, errorCode = errorCode)
