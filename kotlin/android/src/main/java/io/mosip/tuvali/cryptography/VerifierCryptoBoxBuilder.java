package io.mosip.tuvali.cryptography;

import java.security.SecureRandom;

public class VerifierCryptoBoxBuilder {
    public static VerifierCryptoBox build(SecureRandom secureRandom) {
        return new VerifierCryptoBoxImpl(secureRandom);
    }
}

