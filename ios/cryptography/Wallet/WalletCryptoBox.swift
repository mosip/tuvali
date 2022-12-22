import Foundation
import CryptoKit

protocol WalletCryptoBox {
    func buildSecretsTranslator(verifierPublicKey: Data) -> SecretsTranslator
    func getPublicKey() -> Data
}
