package io.mosip.tuvali.wallet.exception

import android.util.Log

class WalletExceptionHandler(val sendError: (String) -> Unit) {
  private val logcat = "WalletExceptionHandler"

  fun handleWalletException(e: Throwable){
    Log.e(logcat,"Wallet got an exception: $e")
    sendError(e.message ?: "Something went wrong in Wallet: ${e.cause}")
  }
}
