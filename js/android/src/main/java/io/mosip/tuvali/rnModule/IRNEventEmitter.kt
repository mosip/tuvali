package io.mosip.tuvali.rnModule

import com.facebook.react.bridge.WritableMap

interface IRNEventEmitter {
  fun emitEvent(eventMap: WritableMap)
}
