package io.mosip.tuvali.exception

// Error Code format
// <Component(2)+Role(1)>(3char)_<Stage>(3char)_<Number>(3char) Eg: TVW-CON-001
// Stage --> CON(Connection) | KEX(Key Exchange) | ENC(Encryption) | TRA(Transfer) | REP(Report) | DEC(Decryption)
// Component+ROLE --> TVW(Tuvali+Wallet) | TVV(Tuvali+Verifier) | TUV(Tuvali where role is unknown)
// UNK --> If stage is not known

enum class ErrorCode(val value: String) {
  UnknownException("TUV_UNK_001"),

  WalletUnknownException("TVW_UNK_001"),
  CentralStateHandlerException("TVW_UNK_002"),
  WalletTransferHandlerException("TVW_UNK_003"),

  VerifierUnknownException("TVV_UNK_001"),
  PeripheralStateHandlerException("TVV_UNK_002"),
  VerifierTransferHandlerException("TVV_UNK_003"),

  InvalidURIException("TVW_CON_001"),
  MTUNegotiationException("TVW_CON_002"),
  ServiceNotFoundException("TVW_CON_003"),
  //TODO: Create specific error codes for the below exception
  TransferFailedException("TVW_REP_001"),

  UnsupportedMTUSizeException("TVV_CON_001"),
  CorruptedChunkReceivedException("TVV_TRA_001"),
  TooManyFailureChunksException("TVV_TRA_002"),
}


