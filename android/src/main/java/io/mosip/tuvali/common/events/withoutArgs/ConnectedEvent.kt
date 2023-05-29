package io.mosip.tuvali.common.events.withoutArgs

import io.mosip.tuvali.common.events.Event

class ConnectedEvent: Event {
  override val type = "onConnected"
}
