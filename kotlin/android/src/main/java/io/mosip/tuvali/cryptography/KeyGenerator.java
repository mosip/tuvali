package io.mosip.tuvali.cryptography;

import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.HKDFBytesGenerator;
import org.bouncycastle.crypto.params.HKDFParameters;
import org.bouncycastle.util.Strings;

import java.util.Arrays;

class KeyGenerator {

    public static final String VERIFIER_INFO="SKVerifier";
    public static final String WALLET_INFO="SKWallet";

    public static byte[] generateKey(byte[] inputKeyMaterial, int outputKeyLength, String infoData) {

        byte[] salt = Strings.toByteArray("SHA-256");
        byte[] info = Strings.toByteArray(infoData);
        byte[] outputKeyMaterial = Arrays.copyOf(inputKeyMaterial, outputKeyLength);

        HKDFParameters params = new HKDFParameters(inputKeyMaterial, salt, info);

        HKDFBytesGenerator hkdf = new HKDFBytesGenerator(new SHA256Digest());
        hkdf.init(params);
        hkdf.generateBytes(outputKeyMaterial, 0, inputKeyMaterial.length);

        return outputKeyMaterial;
    }
}
