package io.mosip.tuvali.openid4vpble.exception.exception

enum class ErrorCodes(val code: Int) {
  InternalStateHandlerException(400),
  InternalTransferHandlerException(401),


  MTUNegotiationException(300),
  TransferFailedException(301),
  CorruptedChunkReceivedException(302),
  TooManyFailureChunksException(303),
  UnsupportedMTUSizeException(304)
}
