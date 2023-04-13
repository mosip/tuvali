package io.mosip.tuvali.common.safeExecute

import io.mosip.tuvali.openid4vpble.exception.OpenIdBLEExceptionHandler
import io.mosip.tuvali.openid4vpble.exception.exception.UnknownException

class TryExecuteSync(private val bleExceptionHandler: OpenIdBLEExceptionHandler) {
  private val mutex = Object()

  fun <T> run(fn: () -> T): T? {
    var returnValue: T? = null;

    synchronized(mutex) {
      try {
        returnValue = fn()
      } catch (e: Exception) {
        bleExceptionHandler.handleException(UnknownException("Caught unknown exception in Try Execute", e))
      }
    }

    return returnValue;
  }
}
