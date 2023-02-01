
import Foundation

@available(iOS 13.0, *)
struct WalletViewModel {
    var advIdentifiers: String?
    var secretTranslators: SecretTranslator?
    var verifierPublicKey: Data?
    var central: Central = Central()

    func isSameAdvIdentifier(advertisementPayload: Data) -> Bool {
        let advIdentiferData = hexStringToData(string: BLEConstants.ADV_IDENTIFIER)
        if advIdentiferData == advertisementPayload {
            return true
        }
        return false
    }

    func hexStringToData(string: String) -> Data {
        let stringArray = Array(string)
        var data: Data = Data()
        for i in stride(from: 0, to: string.count, by: 2) {
            let pair: String = String(stringArray[i]) + String(stringArray[i+1])
                if let byteNum = UInt8(pair, radix: 16) {
                    let byte = Data([byteNum])
                    data.append(byte)
                } else {
                    fatalError()
                }
        }
        return data
    }

    @available(iOS 13.0, *)
    mutating func writeIdentity() {
        print("::: write idendity called ::: ")
        let publicKey = WalletCryptoBoxImpl().getPublicKey()
        print("verifier pub key:::", BLEConstants.verifierPublicKey)
        secretTranslators = WalletCryptoBoxImpl().buildSecretsTranslator(verifierPublicKey: BLEConstants.verifierPublicKey)
        var iv = (secretTranslators?.initializationVector())!
        central.write(serviceUuid: BLEConstants.SERVICE_UUID, charUUID: NetworkCharNums.IDENTIFY_REQUEST_CHAR_UUID, data: iv + publicKey)
     }

    mutating func setAdvIdentifier(advIdentifier: String) {
         //   advIdentifiers = advIdentifier
        BLEConstants.ADV_IDENTIFIER = advIdentifier
    }

    mutating func setSecretTranslator(ss: SecretTranslator, publicKeyData: Data){
            secretTranslators = ss
        BLEConstants.verifierPublicKey = publicKeyData
    }
}

