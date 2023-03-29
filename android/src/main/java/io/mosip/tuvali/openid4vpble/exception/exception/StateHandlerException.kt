package io.mosip.tuvali.openid4vpble.exception.exception

class StateHandlerException(message: String, cause: Throwable): BLEException(message, cause, ErrorCode.InternalStateHandlerException)
