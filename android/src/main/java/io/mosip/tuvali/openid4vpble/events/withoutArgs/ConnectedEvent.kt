package io.mosip.tuvali.openid4vpble.events.withoutArgs

import io.mosip.tuvali.openid4vpble.events.EventWithoutArgs

internal class ConnectedEvent: EventWithoutArgs {
  override val type = "onConnected"
}
