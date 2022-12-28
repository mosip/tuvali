package io.mosip.wallet.transfer.message

class InitResponseTransferMessage(val data: ByteArray): IMessage(TransferMessageTypes.INIT_RESPONSE_TRANSFER)
