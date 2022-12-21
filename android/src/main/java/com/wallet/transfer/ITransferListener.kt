package com.wallet.transfer

@OptIn(ExperimentalUnsignedTypes::class)
interface ITransferListener {
  fun onResponseReceived(data: ByteArray)
  fun onResponseReceivedFailed(errorMsg: String)
}
