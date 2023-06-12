package io.mosip.tuvali.openid4vpble.events.withoutArgs

import io.mosip.tuvali.openid4vpble.events.EventWithoutArgs

class DisconnectedEvent: EventWithoutArgs {
  override val type = "onDisconnected"
}
