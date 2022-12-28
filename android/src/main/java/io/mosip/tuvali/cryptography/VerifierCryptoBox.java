package io.mosip.tuvali.cryptography;

public interface VerifierCryptoBox {
    byte[] publicKey();
    SecretsTranslator buildSecretsTranslator(byte[] initializationVector, byte[] walletPublicKey);
}
