package io.mosip.tuvali.wallet.transfer.message

class ResponseChunkWriteFailureMessage(val err: Int) : IMessage(TransferMessageTypes.RESPONSE_CHUNK_WRITE_FAILURE) {}
