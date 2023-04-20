package io.mosip.tuvali.wallet.transfer

import io.mosip.tuvali.openid4vpble.exception.BLEException

interface ITransferListener {
  fun onResponseSent()
  fun onResponseSendFailure(errorMsg: String)
  fun onException(exception: BLEException)
}
