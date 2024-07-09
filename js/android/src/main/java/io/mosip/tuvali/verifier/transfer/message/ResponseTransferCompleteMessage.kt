package io.mosip.tuvali.verifier.transfer.message

class ResponseTransferCompleteMessage(
  val data: ByteArray,
  val crcFailureCount: Int,
  val totalChunkCount: Int
) : IMessage(TransferMessageTypes.RESPONSE_TRANSFER_COMPLETE) {}
