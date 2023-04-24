package io.mosip.tuvali.exception.wallet

import io.mosip.tuvali.exception.BLEException
import io.mosip.tuvali.exception.ErrorCode

class TransferFailedException(s: String) : BLEException(s, null, ErrorCode.TransferFailedException)
