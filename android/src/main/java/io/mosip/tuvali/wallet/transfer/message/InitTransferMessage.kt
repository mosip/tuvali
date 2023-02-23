package io.mosip.tuvali.wallet.transfer.message

class InitResponseTransferMessage(val data: ByteArray,val maxDataBytes: Int): IMessage(TransferMessageTypes.INIT_RESPONSE_TRANSFER)
