package io.mosip.tuvali.wallet.exception

import android.util.Log
import io.mosip.tuvali.wallet.exception.WalletException

class WalletExceptionHandler(val sendError: (String) -> Unit) {
  private val logcat = "WalletExceptionHandler"

  fun handleException(e: WalletException){
    Log.e(logcat, "Wallet got an Wallet Exception: $e")
    sendError(e.message ?: "Something went wrong in Wallet: ${e.cause}")
  }
}
