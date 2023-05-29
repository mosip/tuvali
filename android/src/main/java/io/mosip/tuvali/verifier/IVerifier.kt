package io.mosip.tuvali.verifier

import io.mosip.tuvali.common.events.Event

interface IVerifier {
  fun startAdvertisement(advIdentifier: String): String
  fun disconnect()
  fun sendVerificationStatus(status: String)
  fun subscribe(consumer: (Event) -> Unit)
  fun unSubscribe()
}
