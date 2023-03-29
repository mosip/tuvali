package io.mosip.tuvali.openid4vpble.exception.exception

enum class ErrorCode(val code: Int) {
  UnknownException(400),
  InternalStateHandlerException(401),
  InternalTransferHandlerException(402),

  MTUNegotiationException(300),
  TransferFailedException(301),
  CorruptedChunkReceivedException(302),
  TooManyFailureChunksException(303),
  UnsupportedMTUSizeException(304)
}
