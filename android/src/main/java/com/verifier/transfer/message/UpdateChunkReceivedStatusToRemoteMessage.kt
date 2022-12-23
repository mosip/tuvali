package com.verifier.transfer.message

class UpdateChunkReceivedStatusToRemoteMessage(val semaphoreCharValue: Int): IMessage(TransferMessageTypes.UPDATE_CHUNK_RECEIVED_STATUS_TO_REMOTE) {}
