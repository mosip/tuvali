package io.mosip.tuvali.verifier.transfer.message

class RequestChunkWriteFailedMessage(val errorMsg: String): IMessage(TransferMessageTypes.REQUEST_CHUNK_WRITE_FAILED) {}
