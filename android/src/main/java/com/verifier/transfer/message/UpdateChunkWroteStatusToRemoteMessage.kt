package com.verifier.transfer.message

class UpdateChunkWroteStatusToRemoteMessage(val semaphoreCharValue: Int): IMessage(TransferMessageTypes.UPDATE_CHUNK_WROTE_STATUS_TO_REMOTE) {}
