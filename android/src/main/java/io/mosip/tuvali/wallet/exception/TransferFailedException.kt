package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCodes

class TransferFailedException(s: String) : WalletException(s, ErrorCodes.TransferFailedException.code)
