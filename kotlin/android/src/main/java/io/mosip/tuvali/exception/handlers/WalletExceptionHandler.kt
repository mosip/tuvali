package io.mosip.tuvali.exception.handlers

import android.util.Log
import io.mosip.tuvali.exception.ErrorCode
import io.mosip.tuvali.exception.ExceptionUtils
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.wallet.exception.WalletException

class WalletExceptionHandler(val sendError: (String, ErrorCode) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: WalletException) {
    val rootCause = ExceptionUtils.getRootBLECause(e)

    Log.e(logTag, "Handling Wallet Exception: ", e)
    sendError(e.message ?: "Something went wrong in Wallet: $rootCause", rootCause.errorCode)
  }
}
