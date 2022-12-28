package io.mosip.verifier.transfer

interface ITransferListener {
  fun onResponseReceived(data: ByteArray)
  fun onResponseReceivedFailed(errorMsg: String)
}
