package io.mosip.tuvali.verifier.exception

import android.util.Log
import io.mosip.tuvali.wallet.exception.WalletException

class VerifierExceptionHandler(val sendError: (String) -> Unit) {
  private val logcat = "VerifierExHandler"

  fun handleException(e: VerifierException){
    Log.e(logcat, "Verifier got an Verifier Exception: $e")
    sendError(e.message ?: "Something went wrong in Verifier: ${e.cause}")
  }
}
