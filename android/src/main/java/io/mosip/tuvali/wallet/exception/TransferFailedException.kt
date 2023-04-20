package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.BLEException
import io.mosip.tuvali.openid4vpble.exception.ErrorCode

class TransferFailedException(s: String) : BLEException(s, null, ErrorCode.TransferFailedException)
