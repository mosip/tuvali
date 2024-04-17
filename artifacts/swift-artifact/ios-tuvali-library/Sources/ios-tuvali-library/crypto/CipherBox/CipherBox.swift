import Foundation

protocol CipherBox {
    func encrypt(message: Data) -> Data
    func decrypt(message: Data) -> Data
}
