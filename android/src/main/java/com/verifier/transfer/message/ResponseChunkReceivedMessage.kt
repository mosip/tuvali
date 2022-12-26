package com.verifier.transfer.message

class ResponseChunkReceivedMessage(val chunkData: ByteArray): IMessage(TransferMessageTypes.RESPONSE_CHUNK_RECEIVED) {}
