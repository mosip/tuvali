package io.mosip.tuvali.openid4vpble.exception.exception


// Error Code format
// <Role>(3char)_<Stage>(3char)_<Number>(3char) Eg: WAL-CON-001
// Stage --> CON(Connection) | KEX(Key Exchange) | ENC(Encryption) | TRA(Transfer) | REP(Report) | DEC(Decryption)
// ROLE --> VER | WAL
// UNK --> If role or stage is not known

enum class ErrorCode(val code: String) {
  UnknownException("UNK_UNK_100"),

  WalletStateHandlerException("WAL_UNK_101"),
  WalletTransferHandlerException("WAL_UNK_102"),

  VerifierStateHandlerException("VER_UNK_101"),
  VerifierTransferHandlerException("VER_UNK_102"),

  MTUNegotiationException("WAL_CON_001"),
  TransferFailedOnVerifierException("WAL_REP_001"),

  UnsupportedMTUSizeException("VER_CON_001"),
  CorruptedChunkReceivedException("VER_TRA_001"),
  TooManyFailureChunksException("VER_TRA_002"),
}


