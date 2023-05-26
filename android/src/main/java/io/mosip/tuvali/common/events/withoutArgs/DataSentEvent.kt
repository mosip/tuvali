package io.mosip.tuvali.common.events.withoutArgs

import io.mosip.tuvali.common.events.EventWithoutArgs

class DataSentEvent: EventWithoutArgs {
  override val type = "onDataSent"
}
