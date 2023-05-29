package io.mosip.tuvali.common.events.withoutArgs

import io.mosip.tuvali.common.events.Event

class SecureChannelEstablishedEvent: Event {
  override val type = "onSecureChannelEstablished"
}
