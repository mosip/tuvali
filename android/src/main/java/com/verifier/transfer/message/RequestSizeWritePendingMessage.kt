package com.verifier.transfer.message

class RequestSizeWritePendingMessage(val size: Int): IMessage(TransferMessageTypes.REQUEST_SIZE_WRITE_PENDING) {}
