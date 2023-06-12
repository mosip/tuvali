package io.mosip.tuvali.openid4vpble.events.withoutArgs

import io.mosip.tuvali.openid4vpble.events.EventWithoutArgs

class SecureChannelEstablishedEvent: EventWithoutArgs {
  override val type = "onSecureChannelEstablished"
}
