package io.mosip.tuvali.verifier.exception

import android.util.Log
import io.mosip.tuvali.openid4vpble.exception.ErrorCode
import io.mosip.tuvali.openid4vpble.exception.ExceptionUtils
import io.mosip.tuvali.transfer.Util

class VerifierExceptionHandler(val sendError: (String, ErrorCode) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: VerifierException){
    val rootCause = ExceptionUtils.getRootBLECause(e)

    Log.e(logTag, "Verifier Exception: $e with root cause $rootCause")
    e.printStackTrace()
    sendError(e.message ?: "Something went wrong in Verifier: $rootCause", rootCause.errorCode)
  }
}
