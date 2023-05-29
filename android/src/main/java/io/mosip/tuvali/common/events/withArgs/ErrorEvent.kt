package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.Event

class ErrorEvent(message: String, code: String): Event {
  override val type = "onError"
  override val args = hashMapOf("message" to message, "code" to code)

}
