package com.verifier.transfer.message

class ResponseTransferFailedMessage(errorMsg: String): IMessage(TransferMessageTypes.RESPONSE_TRANSFER_FAILED) {}
