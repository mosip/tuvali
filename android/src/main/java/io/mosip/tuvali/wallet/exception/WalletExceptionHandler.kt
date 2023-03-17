package io.mosip.tuvali.wallet.exception

import android.util.Log
import io.mosip.tuvali.transfer.Util

class WalletExceptionHandler(val sendError: (String) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: WalletException){
    Log.e(logTag, "Wallet Exception: $e")
    sendError(e.message ?: "Something went wrong in Wallet: ${e.cause}")
  }
}
