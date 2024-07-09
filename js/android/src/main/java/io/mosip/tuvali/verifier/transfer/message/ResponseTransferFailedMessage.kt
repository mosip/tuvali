package io.mosip.tuvali.verifier.transfer.message

class ResponseTransferFailedMessage(val errorMsg: String): IMessage(TransferMessageTypes.RESPONSE_TRANSFER_FAILED) {}
