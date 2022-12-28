package io.mosip.verifier.transfer.message

class ResponseSizeReadSuccessMessage(val responseSize: Int): IMessage(TransferMessageTypes.RESPONSE_SIZE_READ) {}
