package io.mosip.cryptography;

import java.security.SecureRandom;

public class WalletCryptoBoxBuilder {
    public static WalletCryptoBox build(SecureRandom secureRandom) {
        return new WalletCryptoBoxImpl(secureRandom);
    }
}
