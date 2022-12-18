package com.verifier.transfer.message

class UpdateChunkReceivedStatusToRemoteMessage(val semaphoreCharValue: Int): IMessage(TransferMessageTypes.UPDATE_CHUNK_READ_STATUS_TO_REMOTE) {}
