package com.verifier.transfer.message

class ResponseTransferCompleteMessage(val data: ByteArray) : IMessage(TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE) {}
