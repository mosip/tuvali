package io.mosip.tuvali.openid4vpble.exception

open class BLEException(message: String, cause: Throwable?, val errorCode: ErrorCode): Exception(message, cause) {
}
