package io.mosip.wallet.transfer.message

class ResponseTransferFailureMessage(val errorMsg: String): IMessage(TransferMessageTypes.RESPONSE_TRANSFER_FAILED) {}
