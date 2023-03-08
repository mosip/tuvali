import Foundation
import CryptoKit

@available(iOS 13.0, *)
class WalletCryptoBoxImpl: WalletCryptoBox {
    var selfCryptoBox: CryptoBoxImpl = CryptoBoxImpl()
    
    func getPublicKey() -> Data {
        return selfCryptoBox.getPublicKey()
    }
    
    func buildSecretsTranslator(verifierPublicKey: Data) -> SecretTranslator {
        // WalletInfo -> senderInfo -> Sendkey -> myselfPackage
        // VerifierInfo -> receiverInfo -> receiverKey -> OtherPackage -> encrypt (*)
        let secureRandom = secureRandomData(count: CryptoConstants.NONCE_LENGTH)
        let selfCipherPackage = selfCryptoBox.createCipherPackage(otherPublicKey: verifierPublicKey, senderInfo: CryptoConstants.WALLET_INFO, recieverInfo: CryptoConstants.VERIFIER_INFO, nonceBytes: secureRandom)
        return SenderTransferOwnershipOfData(CipherPackage: selfCipherPackage, nonce: secureRandom)
    }
    
    func secureRandomData(count: Int) -> Data {
        var bytes = [Int8](repeating: 0, count: count)
        let status = SecRandomCopyBytes(kSecRandomDefault, count, &bytes)
        if status == errSecSuccess {
            let data = Data(bytes: bytes, count: count)
            return data
        } else {
            return Data()
        }
    }
}
