package io.mosip.tuvali.exception

class ExceptionUtils {
  companion object {
    fun getRootBLECause(e: BLEException): BLEException {
      var cause: BLEException = e
      while (cause.cause != null && cause.cause is BLEException) {
        cause = cause.cause as BLEException
      }
      return cause
    }
  }
}
