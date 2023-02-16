package io.mosip.tuvali.cryptography;

import android.util.Log;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.agreement.X25519Agreement;
import org.bouncycastle.crypto.generators.X25519KeyPairGenerator;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.X25519KeyGenerationParameters;
import org.bouncycastle.crypto.params.X25519PublicKeyParameters;
import org.bouncycastle.util.encoders.Hex;

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
    public CipherPackage createCipherPackage(byte[] otherPublicKey, String senderInfo, String receiverInfo, byte[] ivBytes) {
        //Generate a weak shared secret key
        byte[] weakKey = generateWeakKeyBasedOnX25519(otherPublicKey);

        //Generate two strong shared keys from a weak shared secret key using HKDF algorithm
        byte[] senderKey = KeyGenerator.generateKey(weakKey, SECRET_LENGTH, senderInfo);
        byte[] receiverKey = KeyGenerator.generateKey(weakKey, SECRET_LENGTH, receiverInfo);

        CipherBox self = new CipherBoxImpl(senderKey, ivBytes, NUMBER_OF_MAC_BYTES);
        CipherBox other = new CipherBoxImpl(receiverKey, ivBytes, NUMBER_OF_MAC_BYTES);

        //Print the ephemeral keys
      Log.d("CryptoBox", "self public key: "      + Hex.toHexString(getPublicKey()));
      Log.d("CryptoBox", "iv bytes: "             + Hex.toHexString(ivBytes));
      Log.d("CryptoBox", senderInfo + " key: "    + Hex.toHexString(senderKey));
      Log.d("CryptoBox", receiverInfo + " key: "  + Hex.toHexString(receiverKey));

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
