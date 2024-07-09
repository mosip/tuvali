package io.mosip.tuvali.cryptography;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;

class CipherBoxImpl implements CipherBox {
    private final byte[] initializationVector;
    private KeyParameter secretKey;
    private final int macSizeInBits;

    public CipherBoxImpl(byte[] secretKey, byte[] ivBytes, int digestSizeInBytes) {
        this.secretKey = new KeyParameter(secretKey);
        this.initializationVector = Arrays.clone(ivBytes);
        this.macSizeInBits = digestSizeInBytes * 8;
    }

    @Override
    public byte[] encrypt(byte[] plainText) throws InvalidCipherTextException {
        return process(plainText, true, macSizeInBits);
    }

    @Override
    public byte[] decrypt(byte[] cipherText) throws InvalidCipherTextException {
        return process(cipherText, false, macSizeInBits);
    }

    private byte[] process(byte[] payload, boolean forEncryption, int macSize) throws InvalidCipherTextException {
        GCMBlockCipher gcmBlockCipher = initialiseAESEngineWithGCM(forEncryption, macSize);
        byte[] output = new byte[gcmBlockCipher.getOutputSize(payload.length)];
        int length = gcmBlockCipher.processBytes(payload, 0, payload.length, output, 0);
        length += gcmBlockCipher.doFinal(output, length);
        if (output.length != length)
            throw new RuntimeException("encryption/decryption reported incorrect length");
        return output;
    }

    private GCMBlockCipher initialiseAESEngineWithGCM(boolean forEncryption, int macSize) {
        final AESEngine aesEngine = new AESEngine();
        GCMBlockCipher gcmBlockCipher = new GCMBlockCipher(aesEngine);
        AEADParameters aeadParameters = new AEADParameters(secretKey, macSize, initializationVector);
        gcmBlockCipher.init(forEncryption, aeadParameters);
        return gcmBlockCipher;
    }
}
