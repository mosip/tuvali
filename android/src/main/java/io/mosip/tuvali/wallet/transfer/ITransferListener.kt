package io.mosip.tuvali.wallet.transfer

interface ITransferListener {
  fun onResponseSent()
  fun onResponseSendFailure(errorMsg: String)
  fun onException(exception: Throwable)
}
