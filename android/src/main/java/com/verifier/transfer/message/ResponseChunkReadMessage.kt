package com.verifier.transfer.message

@OptIn(ExperimentalUnsignedTypes::class)
class ResponseChunkReadMessage(val chunkData: UByteArray): IMessage(TransferMessageTypes.RESPONSE_CHUNK_READ) {}
