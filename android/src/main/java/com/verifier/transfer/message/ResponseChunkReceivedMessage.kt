package com.verifier.transfer.message

@OptIn(ExperimentalUnsignedTypes::class)
class ResponseChunkReceivedMessage(val chunkData: UByteArray): IMessage(TransferMessageTypes.RESPONSE_CHUNK_RECEIVED) {}
