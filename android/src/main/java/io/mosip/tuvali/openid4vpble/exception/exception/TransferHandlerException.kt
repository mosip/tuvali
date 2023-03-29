package io.mosip.tuvali.openid4vpble.exception.exception

class TransferHandlerException(message: String, cause: Throwable): BLEException(message, cause, ErrorCode.InternalTransferHandlerException)
