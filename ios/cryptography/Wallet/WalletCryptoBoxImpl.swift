import Foundation
import CryptoKit

class WalletCryptoBoxImpl: WalletCryptoBox {

    var selfCryptoBox: CryptoBoxImpl = CryptoBoxImpl()

    func getPublicKey() -> Data {
        return selfCryptoBox.getPublicKey()
    }

    func buildSecretsTranslator(verifierPublicKey: Data) -> SecretsTranslator {
        let secureRandom = secureRandomData(count: CryptoConstants.INITIALISATION_VECTOR_LENGTH)
        let selfcipherPackage = selfCryptoBox.createCipherPackage(otherPublicKey: verifierPublicKey, senderInfo: CryptoConstants.WALLET_INFO, receiverInfo: CryptoConstants.VERIFIER_INFO, ivBytes: secureRandom)
      return SenderTransfersOwnershipOfData(cipherPackage: selfcipherPackage, initVector: secureRandom)
    }

    func secureRandomData(count: Int) -> Data {
        var bytes = [Int8](repeating: 0, count: count)
        let status = SecRandomCopyBytes(
            kSecRandomDefault,
            count,
            &bytes
        )
        if status == errSecSuccess {
            // Convert bytes to Data
            let data = Data(bytes: bytes, count: count)
            return data
        }
        else {
            return Data()
        }
    }
}
