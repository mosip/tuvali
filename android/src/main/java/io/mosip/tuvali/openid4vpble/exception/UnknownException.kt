package io.mosip.tuvali.openid4vpble.exception

class UnknownException(message: String, cause: Exception): BLEException(message, cause, ErrorCode.UnknownException) {
}
