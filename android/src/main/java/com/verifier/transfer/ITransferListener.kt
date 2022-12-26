package com.verifier.transfer

interface ITransferListener {
  fun onResponseReceived(data: ByteArray)
  fun onResponseReceivedFailed(errorMsg: String)
}
