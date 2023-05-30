package io.mosip.tuvali.exception

open class BLEException(message: String, cause: Exception?, val errorCode: ErrorCode): RuntimeException(message, cause) {
}
