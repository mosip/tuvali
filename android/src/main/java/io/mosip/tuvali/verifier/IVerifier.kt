package io.mosip.tuvali.verifier

interface IVerifier {
  fun startAdvertisement(advIdentifier: String): String
  fun disconnect()
  fun sendVerificationStatus(status: String)
}
