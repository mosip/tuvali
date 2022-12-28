package io.mosip.tuvali.cryptography;

import java.security.SecureRandom;

class VerifierCryptoBoxImpl implements VerifierCryptoBox {
    private CryptoBox selfCryptoBox;

    VerifierCryptoBoxImpl(SecureRandom random) {
        this.selfCryptoBox = new CryptoBoxBuilder().setSecureRandomSeed(random).build();
    }

    @Override
    public byte[] publicKey() {
        return selfCryptoBox.getPublicKey();
    }

    @Override
    public SecretsTranslator buildSecretsTranslator(byte[] initVector, byte[] walletPublicKey) {
        CipherPackage cipherPackage = selfCryptoBox.createCipherPackage(walletPublicKey, KeyGenerator.VERIFIER_INFO, KeyGenerator.WALLET_INFO, initVector);
        return new SenderTransfersOwnershipOfData(initVector, cipherPackage);
    }
}
