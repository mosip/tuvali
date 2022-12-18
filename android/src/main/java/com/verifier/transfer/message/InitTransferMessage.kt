package com.verifier.transfer.message

@OptIn(ExperimentalUnsignedTypes::class)
class InitTransferMessage(val data: UByteArray): IMessage(TransferMessageTypes.INIT_REQUEST_TRANSFER) {}
