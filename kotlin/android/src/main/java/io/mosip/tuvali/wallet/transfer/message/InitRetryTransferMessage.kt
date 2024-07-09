package io.mosip.tuvali.wallet.transfer.message

class InitRetryTransferMessage(val missedSequences: IntArray): IMessage(TransferMessageTypes.INIT_RETRY_TRANSFER)
