package io.mosip.cryptography;

interface CipherBox {
    byte[] encrypt(byte[] plainText);
    byte[] decrypt(byte[] cipherText);
}

