package io.mosip.verifier.transfer.message

class RequestSizeWriteFailedMessage(val errorMsg: String): IMessage(TransferMessageTypes.REQUEST_SIZE_WRITE_FAILED) {}
