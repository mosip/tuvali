package io.mosip.tuvali.openid4vpble.events.withArgs

import io.mosip.tuvali.openid4vpble.events.EventWithArgs


class DataReceivedEvent(val data: String): EventWithArgs {
  override val type = "onDataReceived"

  override fun getData(): HashMap<String, String> {
    return hashMapOf("data" to data)
  }

}
