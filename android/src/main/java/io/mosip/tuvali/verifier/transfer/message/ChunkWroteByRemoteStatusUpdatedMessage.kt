package io.mosip.tuvali.verifier.transfer.message

class ChunkWroteByRemoteStatusUpdatedMessage(val semaphoreCharValue: Int): IMessage(TransferMessageTypes.CHUNK_WROTE_BY_REMOTE_STATUS_UPDATED) {}
