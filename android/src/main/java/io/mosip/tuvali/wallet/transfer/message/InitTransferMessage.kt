package io.mosip.tuvali.wallet.transfer.message

class InitResponseTransferMessage(val data: ByteArray,val mtuSize: Int): IMessage(TransferMessageTypes.INIT_RESPONSE_TRANSFER)
