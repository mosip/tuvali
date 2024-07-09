package io.mosip.tuvali.cryptography;

import org.bouncycastle.crypto.InvalidCipherTextException;

interface CipherBox {
    byte[] encrypt(byte[] plainText) throws InvalidCipherTextException;
    byte[] decrypt(byte[] cipherText) throws InvalidCipherTextException;
}

