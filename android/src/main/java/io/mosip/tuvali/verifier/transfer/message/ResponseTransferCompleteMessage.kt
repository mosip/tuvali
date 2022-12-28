package io.mosip.tuvali.verifier.transfer.message

class ResponseTransferCompleteMessage(val data: ByteArray) : IMessage(TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE) {}
