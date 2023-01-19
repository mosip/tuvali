import Foundation
import Gzip

@objc(Wallet)
@available(iOS 13.0, *)
class Wallet: NSObject {
    
    static let shared = Wallet()
    var central: Central?
    var secretTranslator: SecretTranslator?
    // var viewModel: WalletViewModel = WalletViewModel()
    var advIdentifier: String?
    var verifierPublicKey: Data?
    
    private override init() {}
    
    @objc(getModuleName:withRejecter:)
    func getModuleName(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(["iOS Wallet"])
    }
    
    func setAdvIdentifier(identifier: String) {
        self.advIdentifier = identifier
    }
    
    func registerCallbackForEvent(event: String, callback: @escaping RCTResponseSenderBlock) {
        NotificationCenter.default.addObserver(forName: Notification.Name(rawValue: event), object: nil, queue: nil) { [unowned self] notification in
            print("Handling notification for \(notification.name.rawValue)")
            callback([])
        }
    }
    
    func setSecretTranslator(ss: SecretTranslator, publicKeyData: Data) {
        secretTranslator = ss
        verifierPublicKey = publicKeyData
    }
    
    func isSameAdvIdentifier(advertisementPayload: Data) -> Bool {
        guard let advIdentifier = advIdentifier else {
            print("Found NO ADV Identifier")
            return false
        }
        let advIdentifierData = hexStringToData(string: advIdentifier)
        if advIdentifierData == advertisementPayload {
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

    func sendData(data: String){
        var dataInBytes = Data(data.utf8)
        var compressedBytes = try! dataInBytes.gzipped()
        var encryptedData = secretTranslator?.encryptToSend(data: compressedBytes)
        if (encryptedData != nil) {
            let transferHandler: TransferHandler = TransferHandler(data: encryptedData!)
            let imsgBuilder = imessage(msgType: .INIT_RESPONSE_TRANSFER, data: encryptedData!)
            transferHandler.sendMessage(message: imsgBuilder)
        } else {
            
        }
    }
        @available(iOS 13.0, *)
        func writeIdentity() {
            print("::: write idendity called ::: ")
            let publicKey = WalletCryptoBoxImpl().getPublicKey()
            print("verifier pub key:::", self.verifierPublicKey)
            guard let verifierPublicKey = self.verifierPublicKey else {
                print("Write Identity - Found NO KEY")
                return
            }
            self.secretTranslator = WalletCryptoBoxImpl().buildSecretsTranslator(verifierPublicKey: verifierPublicKey)
            var iv = (self.secretTranslator?.initializationVector())!
            central?.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: TransferService.identifyRequestCharacteristic, data: iv + publicKey)
            NotificationCenter.default.post(name: Notification.Name(rawValue: "EXCHANGE-SENDER-INFO"), object: nil)
        }
    }

    
