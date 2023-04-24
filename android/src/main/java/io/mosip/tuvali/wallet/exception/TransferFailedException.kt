package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class TransferFailedException(s: String) : BLEException(s, null, ErrorCode.TransferFailedException)
