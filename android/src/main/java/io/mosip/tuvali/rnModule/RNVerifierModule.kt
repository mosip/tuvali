package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import io.mosip.tuvali.common.events.IEventEmitter
import io.mosip.tuvali.verifier.Verifier

class RNVerifierModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
  private val eventEmitter: IEventEmitter = EventEmitter(reactContext)
  private val verifier: Verifier = Verifier(reactContext, eventEmitter)

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun startAdvertisement(advIdentifier: String): String {
    return verifier.startAdvertisement(advIdentifier)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun disconnect() {
    verifier.disconnect()
  }

  @ReactMethod
  fun sendVerificationStatus(status: String){
   verifier.sendVerificationStatus(status)
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    var tuvaliVersion: String = "unknown"
    const val NAME = "VerifierModule"
  }
}
