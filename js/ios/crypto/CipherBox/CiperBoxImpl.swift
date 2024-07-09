import Foundation
import CryptoKit

@available(iOS 13.0, *)
class CipherBoxImpl: CipherBox {
    let secretKey: SymmetricKey
    let initializationVector: Data
    let digestSizeInBytes: Int

    init(secretKey: SymmetricKey, initializationVector: Data, digestSizeInBytes: Int) {
        self.secretKey = secretKey
        self.initializationVector = initializationVector
        self.digestSizeInBytes = digestSizeInBytes
    }

    func encrypt(message: Data) -> Data {
        let encryptedSealedBox = try! AES.GCM.seal(message, using: secretKey, nonce: AES.GCM.Nonce(data: initializationVector))
        let cipherWithAuthTag = encryptedSealedBox.ciphertext + encryptedSealedBox.tag
        return cipherWithAuthTag
    }

    func decrypt(message: Data) -> Data {
        let cipherMessage = message.dropLast(digestSizeInBytes)
        let cipherTag = message.suffix(digestSizeInBytes)
        let sealedBox = try! AES.GCM.SealedBox(nonce: AES.GCM.Nonce(data: initializationVector), ciphertext: cipherMessage, tag: cipherTag)
        let decryptedData = try! AES.GCM.open(sealedBox, using: secretKey)
        return decryptedData
    }
}

extension Data {
    func toHex() -> String {
        return self.map { String(format: "%02x", $0)}.joined()
    }
}

@available(iOS 13.0, *)
extension SharedSecret {
    func toData() -> Data {
        return self.withUnsafeBytes { Data(Array($0)) }
    }
}

extension String {
    func hexToString()->String{
        var finalString = ""
        let chars = Array(self)

        for count in stride(from: 0, to: chars.count - 1, by: 2){
            let firstDigit =  Int.init("\(chars[count])", radix: 16) ?? 0
            let lastDigit = Int.init("\(chars[count + 1])", radix: 16) ?? 0
            let decimal = firstDigit * 16 + lastDigit
            let decimalString = String(format: "%c", decimal) as String
            finalString.append(Character.init(decimalString))
        }
        return finalString
    }
    func base64Decoded() -> String? {
        guard let data = Data(base64Encoded: self) else { return nil }
        return String(data: data, encoding: .init(rawValue: 0))
    }
}




