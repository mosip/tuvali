package io.mosip.tuvali.common.events

data class VerificationStatusEvent(val status: VerificationStatus): Event {

  enum class VerificationStatus(val value: Int) {
    ACCEPTED(0),
    REJECTED(1)
  }
}
