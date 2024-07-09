package io.mosip.tuvali.cryptography;

import java.security.SecureRandom;

class CryptoBoxBuilder {

    private SecureRandom secureRandomSeed;
    public CryptoBoxBuilder setSecureRandomSeed(SecureRandom secureRandomSeed) {
        this.secureRandomSeed = secureRandomSeed;
        return this;
    }

    public CryptoBox build(){
        if(secureRandomSeed == null)
            throw new RuntimeException("Cannot create cryptobox without secure random seed");

        return new CryptoBoxImpl(secureRandomSeed);
    }
}
