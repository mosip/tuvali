package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.Event


class DataReceivedEvent(val data: String): Event {
  override val type = "onDataReceived"
  override val args = hashMapOf("data" to data)
}
