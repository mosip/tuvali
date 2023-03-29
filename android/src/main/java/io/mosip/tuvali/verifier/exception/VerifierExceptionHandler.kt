package io.mosip.tuvali.verifier.exception

import android.util.Log
import io.mosip.tuvali.openid4vpble.exception.exception.ErrorCode
import io.mosip.tuvali.transfer.Util

class VerifierExceptionHandler(val sendError: (String, ErrorCode) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: VerifierException){
    Log.e(logTag, "Verifier Exception(#${e.errorCode}): $e")
    Log.e(logTag, "${e.stackTrace}")
    sendError(e.message ?: "Something went wrong in Verifier: ${e.cause}", e.errorCode)
  }
}
