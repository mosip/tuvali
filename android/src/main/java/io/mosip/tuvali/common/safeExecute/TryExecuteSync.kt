package io.mosip.tuvali.common.safeExecute

import io.mosip.tuvali.exception.handlers.OpenIdBLEExceptionHandler

class TryExecuteSync(private val bleExceptionHandler: OpenIdBLEExceptionHandler) {
  private val mutex = Object()

  fun <T> run(fn: () -> T): T? {
    var returnValue: T? = null

    synchronized(mutex) {
      try {
        returnValue = fn()
      } catch (e: Exception) {
        bleExceptionHandler.handleException(e)
      }
    }

    return returnValue
  }
}
