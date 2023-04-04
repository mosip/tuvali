package io.mosip.tuvali.openid4vpble.exception.exception


// Error Code format
// <Grouping>_<Number>
// Groupings :- U -> Unknown, W -> Wallet, V -> Verifier
// Number Groupings :->
//        100-120 -> Unknown Exception Codes
//        121-150 -> Wallet Exception Codes
//        151-180 -> Wallet Exception Codes
enum class ErrorCode(val code: String) {
  UnknownException("U_100"),

  UnknownStateHandlerException("U_101"),
  UnknownTransferHandlerException("U_102"),

  MTUNegotiationException("W_121"),
  TransferFailedOnVerifierException("W_122"),

  CorruptedChunkReceivedException("V_151"),
  TooManyFailureChunksException("V_152"),
  UnsupportedMTUSizeException("V_153"),
}
