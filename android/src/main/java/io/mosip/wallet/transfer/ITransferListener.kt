package io.mosip.wallet.transfer

interface ITransferListener {
  fun onResponseSent()
  fun onResponseSendFailure(errorMsg: String)
}
