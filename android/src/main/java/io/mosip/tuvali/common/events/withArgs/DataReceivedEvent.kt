package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.EventWithArgs


class DataReceivedEvent(val data: String): EventWithArgs {
  override val type = "onDataReceived"

  override fun getData(): HashMap<String, String> {
    return hashMapOf("data" to data)
  }

}
