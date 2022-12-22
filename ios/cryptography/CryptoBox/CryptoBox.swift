import Foundation
import CryptoKit

protocol CryptoBoxProtocol {
    func createCipherPackage(otherPublicKey : Data, senderInfo: String, receiverInfo:String, ivBytes: Data) -> CipherPackage
    func getPublicKey() -> Data
}
