package io.mosip.tuvali.exception.handlers

import android.util.Log
import io.mosip.tuvali.exception.ErrorCode
import io.mosip.tuvali.exception.ExceptionUtils
import io.mosip.tuvali.transfer.Util
import io.mosip.tuvali.verifier.exception.VerifierException

class VerifierExceptionHandler(val sendError: (String, ErrorCode) -> Unit) {
  private val logTag = Util.getLogTag(javaClass.simpleName)

  fun handleException(e: VerifierException) {
    val rootCause = ExceptionUtils.getRootBLECause(e)
    val crcFailureCount = ExceptionUtils.getCRCFailureCount(e)
    val totalChunkCount = ExceptionUtils.getTotalChunkCount(e)

    Log.e(logTag, "Handling Verifier Exception: ", e)

    val crcErrorLogString = if (crcFailureCount != null)
      "CRCFailureCount:$crcFailureCount TotalChunkCount:$totalChunkCount -"
    else ""

    val errorMessage = if (e.message != null)
      crcErrorLogString + e.message
    else
      crcErrorLogString + "Something went wrong in Verifier: $rootCause"
    sendError(
      errorMessage,
      rootCause.errorCode
    )
  }
}
