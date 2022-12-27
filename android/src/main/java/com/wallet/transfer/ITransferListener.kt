package com.wallet.transfer

interface ITransferListener {
  fun onResponseSent()
  fun onResponseSendFailure(errorMsg: String)
}
