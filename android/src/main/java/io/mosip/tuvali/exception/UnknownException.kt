package io.mosip.tuvali.exception

class UnknownException(message: String, cause: Exception): BLEException(message, cause, ErrorCode.UnknownException) {
}
