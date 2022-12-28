import Foundation
import CryptoKit

protocol CryptoBox {
    func createCipherPackage(otherPublicKey: Data, senderInfo: String, recieverInfo: String, ivBytes: Data) -> CipherPackage
    func getPublicKey() -> Data
}

