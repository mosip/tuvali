package io.mosip.tuvali.cryptography;

import org.bouncycastle.crypto.InvalidCipherTextException;

/*
vk: verifier key
wk: wallet key

The sender transfers the ownership of the data, so the sender should use the receivers key to encrypt
wallet sending data to verifier | w->v: wallet encrypts with vk and verifier decrypts with vk
verifier sending data to wallet | v->w: verifier encrypts with wk and wallet decrypts with wk
 */
class SenderTransfersOwnershipOfData implements SecretsTranslator {
    private byte[] initVector;
    private CipherBox senderCipherBox;
    private CipherBox receiverCipherBox;

    public SenderTransfersOwnershipOfData(byte[] initVector, CipherPackage cipherPackage) {
        this.initVector = initVector;
        this.senderCipherBox = cipherPackage.getSelf();
        this.receiverCipherBox = cipherPackage.getOther();
    }

    @Override
    public byte[] initializationVector() {
        return initVector;
    }

    @Override
    public byte[] encryptToSend(byte[] plainText) throws InvalidCipherTextException {
        byte[] encrypt = receiverCipherBox.encrypt(plainText);
        return encrypt;
    }

    @Override
    public byte[] decryptUponReceive(byte[] cipherText) throws InvalidCipherTextException {
        byte[] plainText = senderCipherBox.decrypt(cipherText);
        return plainText;
    }
}
