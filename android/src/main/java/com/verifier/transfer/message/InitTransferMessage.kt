package com.verifier.transfer.message

class InitTransferMessage(val data: ByteArray): IMessage(TransferMessageTypes.INIT_REQUEST_TRANSFER) {}
