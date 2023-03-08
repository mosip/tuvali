import Foundation

protocol SecretTranslator {
    func getNonce() -> Data
    func encryptToSend(data: Data) -> Data
    func decryptUponReceive(data: Data) -> Data
}
