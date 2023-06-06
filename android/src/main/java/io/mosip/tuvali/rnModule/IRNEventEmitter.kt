package io.mosip.tuvali.rnModule

import io.mosip.tuvali.common.events.Event

interface IRNEventEmitter {
  fun emitEvent(event: Event)
}
