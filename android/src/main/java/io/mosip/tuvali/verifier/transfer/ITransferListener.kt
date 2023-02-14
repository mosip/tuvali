package io.mosip.tuvali.verifier.transfer

import java.util.UUID

interface ITransferListener {
  fun sendDataOverNotification(charUUID: UUID, data: ByteArray)
  fun onResponseReceived(data: ByteArray)
  fun onResponseReceivedFailed(errorMsg: String)
  fun onException(exception: Throwable )
}
