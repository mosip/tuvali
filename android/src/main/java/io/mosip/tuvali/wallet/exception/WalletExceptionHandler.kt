package io.mosip.tuvali.wallet.exception

import android.util.Log
import io.mosip.tuvali.openid4vpble.exception.ErrorCode
import io.mosip.tuvali.openid4vpble.exception.ExceptionUtils
import io.mosip.tuvali.transfer.Util

class WalletExceptionHandler(val sendError: (String, ErrorCode) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: WalletException) {
    val rootCause = ExceptionUtils.getRootBLECause(e)

    Log.e(logTag, "Handling Wallet Exception: ", e)
    sendError(e.message ?: "Something went wrong in Wallet: $rootCause", rootCause.errorCode)
  }
}
