package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule

private const val EVENT_NAME = "DATA_EVENT"

class RNEventEmitter(private val reactContext: ReactApplicationContext): IRNEventEmitter {
  override fun emitEvent(eventMap: WritableMap) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(EVENT_NAME, eventMap)
  }
}
