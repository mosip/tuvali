package io.mosip.tuvali.wallet.exception

import android.util.Log
import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode
import io.mosip.tuvali.transfer.Util

class WalletExceptionHandler(val sendError: (String, ErrorCode) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: WalletException){
    Log.e(logTag, "Wallet Exception(#${e.errorCode}): $e")
    Log.e(logTag, "${e.stackTrace}")
    sendError(e.message ?: "Something went wrong in Wallet: ${e.cause}", e.errorCode)
  }
}
