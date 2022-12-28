package io.mosip.tuvali.cryptography;

interface CipherBox {
    byte[] encrypt(byte[] plainText);
    byte[] decrypt(byte[] cipherText);
}

