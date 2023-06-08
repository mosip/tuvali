package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.WritableMap
import io.mosip.tuvali.common.events.Event

interface IRNEventEmitter {
  fun emitEvent(eventMap: WritableMap)
}
