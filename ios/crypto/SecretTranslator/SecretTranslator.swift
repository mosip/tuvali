import Foundation

protocol SecretTranslator {
    func initializationVector() -> Data
    func encryptToSend(data: Data) -> Data
    func decryptUponReceive(data: Data) -> Data
}
