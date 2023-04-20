package io.mosip.tuvali.verifier.exception

import android.util.Log
import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode
import io.mosip.tuvali.openid4vpble.exception.StateHandlerException
import io.mosip.tuvali.openid4vpble.exception.TransferHandlerException
import io.mosip.tuvali.transfer.Util

class VerifierExceptionHandler(val sendError: (String, ErrorCode) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: VerifierException){
    when (e.cause?.javaClass?.simpleName) {
      StateHandlerException::class.simpleName -> Log.e(logTag, "Verifier State Handler Exception: ${e.cause}")
      TransferHandlerException::class.simpleName -> Log.e(logTag, "Verifier Transfer Handler Exception: ${e.cause}")
      else -> Log.e(logTag, "Verifier Exception: $e")
    }
    sendError(e.message ?: "Something went wrong in Verifier: ${e.cause}", e.errorCode)
  }
}
