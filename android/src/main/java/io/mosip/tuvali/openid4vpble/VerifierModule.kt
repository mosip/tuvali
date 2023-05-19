package io.mosip.tuvali.openid4vpble

import android.util.Log
import com.facebook.react.bridge.*
import io.mosip.tuvali.common.safeExecute.TryExecuteSync
import io.mosip.tuvali.exception.ErrorCode
import io.mosip.tuvali.exception.handlers.OpenIdBLEExceptionHandler
import io.mosip.tuvali.openid4vpble.events.withArgs.ErrorEvent
import io.mosip.tuvali.openid4vpble.events.withArgs.VerificationStatusEvent
import io.mosip.tuvali.openid4vpble.events.withoutArgs.DisconnectedEvent
import io.mosip.tuvali.transfer.Util.Companion.getLogTag
import io.mosip.tuvali.verifier.Verifier

class VerifierModule(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val eventEmitter = EventEmitter(reactContext)
  private val logTag = getLogTag(javaClass.simpleName)
  private var verifier: Verifier? = null
  private var bleExceptionHandler = OpenIdBLEExceptionHandler(eventEmitter::emitError, this::stopBLE)
  private val tryExecuteSync = TryExecuteSync(bleExceptionHandler)


  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setTuvaliVersion(version: String) {
    tuvaliVersion = version
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun startAdvertisement(advIdentifier: String): String {
    Log.d(logTag, "startAdvertisement called with advIdentifier $advIdentifier at ${System.nanoTime()}")

    return tryExecuteSync.run {

      if (verifier == null) {
        initializeVerifier()
      }

      val payload = verifier?.getAdvPayloadInHex(advIdentifier)
      Log.d(logTag, "synchronized startAdvertisement called with adv identifier $payload at ${System.nanoTime()} and verifier hashcode: ${verifier.hashCode()}")

      verifier?.startAdvertisement(advIdentifier)

      return@run "OPENID4VP://$payload"
    }.orEmpty()
  }

  private fun initializeVerifier() {
    Log.d(logTag, "Initializing new verifier object at ${System.nanoTime()}")
    verifier = Verifier(reactContext, eventEmitter, bleExceptionHandler::handleException)
    verifier?.generateKeyPair()
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun disconnect() {
    //TODO: Make sure callback can be called only once[Can be done once wallet and verifier split into different modules]
    Log.d(logTag, "destroyConnection called at ${System.nanoTime()}")
    tryExecuteSync.run {
      stopBLE { eventEmitter.emitEventWithoutArgs(DisconnectedEvent()) }
    }
  }

  private fun stopBLE(callback: () -> Unit) {
    if (verifier == null) {
      callback()
    } else {
      Log.d(logTag, "synchronized destroyConnection called for verifier at ${System.nanoTime()}")
      stopVerifier { callback() }
    }
  }

  private fun stopVerifier(callback: () -> Unit) {
    try {
      verifier?.stop(callback)
    } finally {
      Log.d(logTag, "stopVerifier: setting to null")
      verifier = null
    }
  }

  @ReactMethod
  fun sendVerificationStatus(status: String){
    Log.d(logTag, "sendVerificationStatus status $status at ${System.nanoTime()}")

    tryExecuteSync.run {
      verifier?.notifyVerificationStatus(status == VerificationStatusEvent.VerificationStatus.ACCEPTED.value)
    }
  }

  companion object {
    var tuvaliVersion: String = "unknown"
    const val NAME = "VerifierModule"
  }
}
