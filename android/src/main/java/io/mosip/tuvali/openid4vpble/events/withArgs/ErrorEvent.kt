package io.mosip.tuvali.openid4vpble.events.withArgs

import io.mosip.tuvali.openid4vpble.events.EventWithArgs

class ErrorEvent(private val message: String, private val code: String): EventWithArgs {
  override fun getData(): HashMap<String, String> {
    return hashMapOf("message" to message, "code" to code)
  }

  override val type = "onError"

}
