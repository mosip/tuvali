package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.EventWithArgs

class ErrorEvent(private val message: String, private val code: String): EventWithArgs {
  override fun getData(): HashMap<String, String> {
    return hashMapOf("message" to message, "code" to code)
  }

  override val type = "onError"

}
