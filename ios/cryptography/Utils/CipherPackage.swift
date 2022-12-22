import Foundation

class CipherPackage {
   private var myself: CipherBox
   private var other: CipherBox

    init(myself: CipherBox, other: CipherBox) {
        self.myself = myself
        self.other = other
    }

    var getSelfCipherBox: CipherBox {
        return myself
    }

    var getOtherCipherBox: CipherBox {
        return other
    }
}
