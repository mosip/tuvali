package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode

class TransferFailedException(s: String) : WalletException(s, ErrorCode.TransferFailedException)
