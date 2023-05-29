package io.mosip.tuvali.common.events.withoutArgs

import io.mosip.tuvali.common.events.Event

class DataSentEvent: Event {
  override val type = "onDataSent"
}
