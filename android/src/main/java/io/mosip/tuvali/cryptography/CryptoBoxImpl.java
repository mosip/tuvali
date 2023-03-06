package io.mosip.tuvali.cryptography;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;

import java.security.SecureRandom;

class CryptoBoxImpl implements CryptoBox {

    int SECRET_LENGTH = 32;
    int NUMBER_OF_MAC_BYTES = 16; //16 Bytes of MAC Digest

    private final AsymmetricCipherKeyPair keyPair;

    public CryptoBoxImpl(SecureRandom randomSeed) {
        AsymmetricCipherKeyPairGenerator kpGen = new X25519KeyPairGenerator();
        kpGen.init(new X25519KeyGenerationParameters(randomSeed));
        keyPair = kpGen.generateKeyPair();
    }

    @Override
    public byte[] getPublicKey() {
        final AsymmetricKeyParameter aPublic = keyPair.getPublic();
        final X25519PublicKeyParameters keyParameters = (X25519PublicKeyParameters) aPublic;
        return keyParameters.getEncoded();
    }

    @Override
    public CipherPackage createCipherPackage(byte[] otherPublicKey, String senderInfo, String receiverInfo, byte[] nonceBytes) {
        //Generate a weak shared secret key
        byte[] weakKey = generateWeakKeyBasedOnX25519(otherPublicKey);

        //Generate two strong shared keys from a weak shared secret key using HKDF algorithm
        byte[] senderKey = KeyGenerator.generateKey(weakKey, SECRET_LENGTH, senderInfo);
        byte[] receiverKey = KeyGenerator.generateKey(weakKey, SECRET_LENGTH, receiverInfo);

        CipherBox self = new CipherBoxImpl(senderKey, nonceBytes, NUMBER_OF_MAC_BYTES);
        CipherBox other = new CipherBoxImpl(receiverKey, nonceBytes, NUMBER_OF_MAC_BYTES);

        return new CipherPackage(self, other);
    }

    private byte[] generateWeakKeyBasedOnX25519(byte[] otherPublicKey) {
        X25519Agreement keyAgreement = new X25519Agreement();
        keyAgreement.init(keyPair.getPrivate());
        byte[] weakSharedSecret = new byte[keyAgreement.getAgreementSize()];
        keyAgreement.calculateAgreement(new X25519PublicKeyParameters(otherPublicKey, 0), weakSharedSecret, 0);
        return weakSharedSecret;
    }
}
