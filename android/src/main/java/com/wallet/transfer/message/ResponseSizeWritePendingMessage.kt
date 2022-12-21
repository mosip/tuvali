package com.wallet.transfer.message

class ResponseSizeWritePendingMessage(val size: Int): IMessage(TransferMessageTypes.RESPONSE_SIZE_WRITE_PENDING) {}
