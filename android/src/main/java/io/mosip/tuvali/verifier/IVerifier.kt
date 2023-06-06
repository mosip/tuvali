package io.mosip.tuvali.verifier

import io.mosip.tuvali.common.events.Event

interface IVerifier {
  fun startAdvertisement(advIdentifier: String): String
  fun disconnect()
  fun sendVerificationStatus(status: Int)
  fun subscribe(listener: (Event) -> Unit)
  fun unSubscribe()
}
