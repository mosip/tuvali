package com.cryptography;

interface CipherBox {
    byte[] encrypt(byte[] plainText);
    byte[] decrypt(byte[] cipherText);
}

