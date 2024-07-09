package io.mosip.tuvali.cryptography;

import org.bouncycastle.crypto.InvalidCipherTextException;

public interface SecretsTranslator {
    byte[] getNonce();
    byte[] encryptToSend(byte[] plainText) throws InvalidCipherTextException;
    byte[] decryptUponReceive(byte[] cipherText) throws InvalidCipherTextException;
}
