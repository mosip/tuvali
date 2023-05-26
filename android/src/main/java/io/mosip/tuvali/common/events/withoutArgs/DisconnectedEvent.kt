package io.mosip.tuvali.common.events.withoutArgs

import io.mosip.tuvali.common.events.EventWithoutArgs

class DisconnectedEvent: EventWithoutArgs {
  override val type = "onDisconnected"
}
