package io.mosip.tuvali.verifier.transfer.message

class RequestSizeWriteFailedMessage(val errorMsg: String): IMessage(TransferMessageTypes.REQUEST_SIZE_WRITE_FAILED) {}
