package com.verifier

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class VerifierModule(reactContext: ReactApplicationContext):
  ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  fun getModuleName(promise: Promise): Unit {
    promise.resolve("Android Verifier")
  }

  companion object {
    const val NAME = "Verifier"
  }
}
