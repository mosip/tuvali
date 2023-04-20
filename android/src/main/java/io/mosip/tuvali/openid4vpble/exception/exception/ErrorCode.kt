package io.mosip.tuvali.openid4vpble.exception.exception

// Error Code format
// <Component(2)+Role(1)>(3char)_<Stage>(3char)_<Number>(3char) Eg: TVW-CON-001
// Stage --> CON(Connection) | KEX(Key Exchange) | ENC(Encryption) | TRA(Transfer) | REP(Report) | DEC(Decryption)
// ROLE --> TVW(Tuvali+Wallet) | TVV(Tuvali+Verifier)
// UNK --> If role or stage is not known

enum class ErrorCode(val code: String) {
  UnknownException("UNK_UNK_001"),

  WalletUnknownException("TVW_UNK_001"),
  WalletStateHandlerException("TVW_UNK_002"),
  WalletTransferHandlerException("TVW_UNK_003"),

  VerifierUnknownException("TVV_UNK_001"),
  VerifierStateHandlerException("TVV_UNK_002"),
  VerifierTransferHandlerException("TVV_UNK_003"),

  MTUNegotiationException("TVW_CON_001"),
  //TODO: Create specific error codes for the below exception
  TransferFailedException("TVW_REP_001"),

  UnsupportedMTUSizeException("TVV_CON_001"),
  CorruptedChunkReceivedException("TVV_TRA_001"),
  TooManyFailureChunksException("TVV_TRA_002"),
}


