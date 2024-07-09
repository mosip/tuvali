package io.mosip.tuvali.exception

open class BLEException(
  message: String,
  cause: Exception?,
  val errorCode: ErrorCode,
  val crcFailureCount: Int? = null,
  val totalChunkCount: Int? = null,
) : RuntimeException(message, cause) {
}
