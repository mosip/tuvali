package com.cryptography;

public interface WalletCryptoBox {
    byte[] publicKey();
    SecretsTranslator buildSecretsTranslator(byte[] walletPublicKey);
}
