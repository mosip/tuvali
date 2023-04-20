package io.mosip.tuvali.openid4vpble.exception

open class BLEException(message: String, cause: Exception?, val errorCode: ErrorCode): RuntimeException(message, cause) {
}
