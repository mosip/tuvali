package io.mosip.tuvali.wallet.transfer.message

class ChunkWriteToRemoteStatusUpdatedMessage(val semaphoreCharValue: Int): IMessage(TransferMessageTypes.CHUNK_WRITE_TO_REMOTE_STATUS_UPDATED) {}
