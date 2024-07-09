package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import io.mosip.tuvali.common.version.VersionDetails

class RNVersionModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  override fun getName(): String {
    return NAME
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun setTuvaliVersion(version: String) {
    VersionDetails.tuvaliVersion = version
  }

  companion object {
    const val NAME = "VersionModule"
  }
}
