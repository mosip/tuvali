package io.mosip.tuvali.cryptography;

import java.security.SecureRandom;

class WalletCryptoBoxImpl implements WalletCryptoBox {
    private final CryptoBox selfCryptoBox;
    private SecureRandom secureRandom;

    WalletCryptoBoxImpl(SecureRandom random) {
        this.selfCryptoBox = new CryptoBoxBuilder().setSecureRandomSeed(random).build();
        this.secureRandom = random;
    }

    @Override
    public byte[] publicKey() {
        return selfCryptoBox.getPublicKey();
    }

    @Override
    public SecretsTranslator buildSecretsTranslator(byte[] verifierPublicKey) {
        byte[] nonceBytes = new byte[CryptoBox.NONCE_LENGTH];
        secureRandom.nextBytes(nonceBytes);

        CipherPackage cipherPackage = selfCryptoBox.createCipherPackage(verifierPublicKey, KeyGenerator.WALLET_INFO, KeyGenerator.VERIFIER_INFO, nonceBytes);
        return new SenderTransfersOwnershipOfData(nonceBytes, cipherPackage);
    }
}
