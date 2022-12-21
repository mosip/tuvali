package com.verifier.transfer.message

@OptIn(ExperimentalUnsignedTypes::class)
class ResponseTransferCompleteMessage(val data: UByteArray) : IMessage(TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE) {}
