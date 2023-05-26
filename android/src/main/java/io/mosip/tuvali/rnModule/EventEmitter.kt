package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import io.mosip.tuvali.common.events.EventWithArgs
import io.mosip.tuvali.common.events.EventWithoutArgs
import io.mosip.tuvali.common.events.IEventEmitter
import io.mosip.tuvali.exception.ErrorCode
import io.mosip.tuvali.common.events.withArgs.ErrorEvent

private const val EVENT_NAME = "DATA_EVENT"

class EventEmitter(private val reactContext: ReactApplicationContext): IEventEmitter {

  override fun emitEventWithoutArgs(event: EventWithoutArgs) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", event.type)
    emitEvent(writableMap)
  }

  override fun emitEventWithArgs(event: EventWithArgs) {
    val writableMap = Arguments.createMap()
    writableMap.putString("type", event.type)
    event.getData().entries.map { entry -> writableMap.putString(entry.key, entry.value) }
    emitEvent(writableMap)
  }

  override fun emitError(message: String, code: ErrorCode) {
    emitEventWithArgs(ErrorEvent(message, code.value))
  }

  private fun emitEvent(data: WritableMap?) {
    reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(EVENT_NAME, data)
  }
}
