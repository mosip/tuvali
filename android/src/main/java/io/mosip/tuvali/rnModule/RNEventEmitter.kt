package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.mosip.tuvali.common.events.Event

private const val EVENT_NAME = "DATA_EVENT"

class RNEventEmitter(private val reactContext: ReactApplicationContext) {

  fun emitEvent(event: Event) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", event.type)
    event.args?.entries?.map { entry -> writableMap.putString(entry.key, entry.value) }
    emitEvent(writableMap)
  }

  private fun emitEvent(data: WritableMap?) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(EVENT_NAME, data)
  }
}
