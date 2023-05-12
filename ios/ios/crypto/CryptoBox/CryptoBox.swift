import Foundation
import CryptoKit

protocol CryptoBox {
    func createCipherPackage(otherPublicKey: Data, senderInfo: String, recieverInfo: String, nonceBytes: Data) -> CipherPackage
    func getPublicKey() -> Data
}

