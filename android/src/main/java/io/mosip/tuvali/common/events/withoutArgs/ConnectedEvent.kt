package io.mosip.tuvali.common.events.withoutArgs

import io.mosip.tuvali.common.events.EventWithoutArgs

internal class ConnectedEvent: EventWithoutArgs {
  override val type = "onConnected"
}
