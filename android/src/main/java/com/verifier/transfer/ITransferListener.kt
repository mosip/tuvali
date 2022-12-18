package com.verifier.transfer

@OptIn(ExperimentalUnsignedTypes::class)
interface ITransferListener {
  fun onResponseReceived(data: UByteArray)
}
