package io.mosip.tuvali.common.events.withoutArgs

import io.mosip.tuvali.common.events.EventWithoutArgs

class SecureChannelEstablishedEvent: EventWithoutArgs {
  override val type = "onSecureChannelEstablished"
}
