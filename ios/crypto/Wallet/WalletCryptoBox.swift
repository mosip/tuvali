import Foundation
import CryptoKit

protocol WalletCryptoBox {
    func buildSecretsTranslator(verifierPublicKey: Data) -> SecretTranslator
    func getPublicKey() -> Data
}
