package io.mosip.tuvali.openid4vpble.exception

class UnknownException(message: String, cause: Throwable): BLEException(message, cause, ErrorCode.UnknownException) {
}
