package io.mosip.tuvali.cryptography;

public class CipherPackage {
    private CipherBox self;
    private CipherBox other;

    public CipherPackage(CipherBox self, CipherBox other) {
        this.self = self;
        this.other = other;
    }

    public CipherBox getSelf() {
        return self;
    }

    public CipherBox getOther() {
        return other;
    }
}
