package io.mosip.tuvali.openid4vpble.exception.exception

open class BLEException(message: String, cause: Throwable?, val errorCode: ErrorCode): Throwable(message, cause) {
}
