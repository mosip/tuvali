package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.EventWithArgs

class VerificationStatusEvent(private val status: VerificationStatus): EventWithArgs {
  override fun getData(): HashMap<String, String> {
    return hashMapOf("status" to status.value)
  }

  override val type = "onVerificationStatusReceived"

  enum class VerificationStatus(val value: String) {
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED")
  }
}
