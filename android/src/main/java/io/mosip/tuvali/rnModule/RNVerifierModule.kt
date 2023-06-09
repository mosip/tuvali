package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import io.mosip.tuvali.verifier.Verifier

class RNVerifierModule(
  private val eventEmitter: RNEventEmitter,
  private val verifier: Verifier,
  reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext) {

  init {
    verifier.subscribe {
      eventEmitter.emitEvent(RNEventMapper.toMap(it))
    }
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun startAdvertisement(advIdentifier: String): String {
    return verifier.startAdvertisement(advIdentifier)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun disconnect() {
    verifier.disconnect()
  }

  @ReactMethod
  fun sendVerificationStatus(status: Int) {
    verifier.sendVerificationStatus(status)
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "VerifierModule"
  }
}
