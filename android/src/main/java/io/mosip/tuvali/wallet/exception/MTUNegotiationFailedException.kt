package io.mosip.tuvali.wallet.exception

import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCodes

class MTUNegotiationFailedException(s: String) : WalletException(s, ErrorCodes.MTUNegotiationException.code)
