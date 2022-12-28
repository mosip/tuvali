package io.mosip.tuvali.cryptography;

interface CryptoBox {
    int INITIALISATION_VECTOR_LENGTH = 12;
    byte[] getPublicKey();
    CipherPackage createCipherPackage(byte[] otherPublicKey, String selfInfo, String receipientInfo, byte[] ivBytes);
}



