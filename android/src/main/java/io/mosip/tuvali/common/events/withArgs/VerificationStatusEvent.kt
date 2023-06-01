package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.Event

data class VerificationStatusEvent(val status: VerificationStatus): Event {

  enum class VerificationStatus(val value: String) {
    ACCEPTED("ACCEPTED"),
    REJECTED("REJECTED")
  }
}
