import Foundation
import CryptoKit

@available(iOS 13.0, *)
class CryptoBoxImpl: CryptoBox {
    var privateKey: Curve25519.KeyAgreement.PrivateKey
    
    init() {
        self.privateKey = Curve25519.KeyAgreement.PrivateKey()
    }
    
    func getPublicKey() -> Data {
        return privateKey.publicKey.rawRepresentation
    }
    
    func createCipherPackage(otherPublicKey: Data, senderInfo: String, recieverInfo: String, ivBytes: Data) -> CipherPackage {
        let sharedPublicKey = try! Curve25519.KeyAgreement.PublicKey(rawRepresentation: otherPublicKey)
        let weakKey = try! privateKey.sharedSecretFromKeyAgreement(with: sharedPublicKey)
        let strongKey = KeyGenerator().generateStrongKeyBasedOnHKDF(sharedSecretKey: weakKey, keyLength: CryptoConstants.SECRET_LENTGH, infoData: senderInfo)
        let senderKey = KeyGenerator().generateStrongKeyBasedOnHKDF(sharedSecretKey: weakKey, keyLength: CryptoConstants.SECRET_LENTGH, infoData: senderInfo)
        let recieverKey = KeyGenerator().generateStrongKeyBasedOnHKDF(sharedSecretKey: weakKey, keyLength: CryptoConstants.SECRET_LENTGH, infoData: recieverInfo)
        let myselfCipherPackage = CipherBoxImpl(secretKey: senderKey, initializationVector: ivBytes, digestSizeInBytes: CryptoConstants.NUMBER_OF_MAC_BYTES)
        let otherCipherPackage = CipherBoxImpl(secretKey: recieverKey, initializationVector: ivBytes, digestSizeInBytes: CryptoConstants.NUMBER_OF_MAC_BYTES)
        
        return CipherPackage(myself: myselfCipherPackage, other: otherCipherPackage)
    }
    
}
