package io.mosip.tuvali.openid4vpble

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.mosip.tuvali.exception.ErrorCode

class ReactUtils(private val reactContext: ReactApplicationContext) {

  private fun emitEvent(eventName: String, data: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, data)
  }

    fun emitNearbyMessage(eventType: String, data: String) {
    val writableMap = Arguments.createMap()
    writableMap.putString("data", "$eventType\n${data}")
    writableMap.putString("type", "msg")
    emitEvent("EVENT_NEARBY", writableMap)
  }

   fun emitNearbyErrorEvent(message: String, errorCode: ErrorCode) {
    val writableMap = Arguments.createMap()
    writableMap.putString("message", message)
    writableMap.putString("code", errorCode.code.toString())
    writableMap.putString("type", "onError")
    emitEvent("EVENT_NEARBY", writableMap)
  }

    fun emitNearbyEvent(eventType: String) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", eventType)
    emitEvent("EVENT_NEARBY", writableMap)
  }

  private fun emitLogEvent(eventType: String, data: Map<String, String>) {}
}
