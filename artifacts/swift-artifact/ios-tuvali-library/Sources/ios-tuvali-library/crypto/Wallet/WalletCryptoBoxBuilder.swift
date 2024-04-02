import Foundation

@available(iOS 13.0, *)
class WalletCryptoBoxBuilder {
    func build() -> WalletCryptoBox {
        return WalletCryptoBoxImpl()
    }
}
