package io.mosip.tuvali.common.safeExecute

import io.mosip.tuvali.openid4vpble.exception.OpenIdBLEExceptionHandler
import io.mosip.tuvali.openid4vpble.exception.exception.UnknownException

class SafeExecute(private val bleExceptionHandler: OpenIdBLEExceptionHandler) {
  private val mutex = Object()

  fun <T> run(fn: () -> T): T? {
    var returnValue: T? = null;

    synchronized(mutex) {
      try {
        returnValue = fn()
      } catch (e: Exception) {
        bleExceptionHandler.handleException(UnknownException("Unknown Exception", e))
      }
    }

    return returnValue;
  }
}
