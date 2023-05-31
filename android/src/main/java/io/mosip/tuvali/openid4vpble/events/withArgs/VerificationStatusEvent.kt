package io.mosip.tuvali.openid4vpble.events.withArgs

import io.mosip.tuvali.openid4vpble.events.EventWithArgs

class VerificationStatusEvent(private val status: VerificationStatus): EventWithArgs {
  override fun getData(): HashMap<String, String> {
    return hashMapOf("status" to status.value.toString())
  }

  override val type = "onVerificationStatusReceived"

  enum class VerificationStatus(val value: Int) {
    ACCEPTED(0),
    REJECTED(1)
  }
}
