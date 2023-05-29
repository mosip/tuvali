package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.Event

class VerificationStatusEvent(private val status: VerificationStatus): Event {
  override val args = hashMapOf("status" to status.value)

  override val type = "onVerificationStatusReceived"

  enum class VerificationStatus(val value: String) {
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED")
  }
}
