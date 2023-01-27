import Foundation
import Gzip

@objc(Wallet)
@available(iOS 13.0, *)
class Wallet: NSObject {
    
    static let shared = Wallet()
    var central: Central?
    var secretTranslator: SecretTranslator?
    var cryptoBox: WalletCryptoBox = WalletCryptoBoxBuilder().build()
    var advIdentifier: String?
    var verifierPublicKey: Data?
    static let EXCHANGE_RECEIVER_INFO_DATA = "{\"deviceName\":\"wallet\"}"
    
    private override init() {
        super.init()
        lookForDestroyConnection()
    }
    
    @objc(getModuleName:withRejecter:)
    func getModuleName(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(["iOS Wallet"])
    }
    
    func setAdvIdentifier(identifier: String) {
        self.advIdentifier = identifier
    }
    
    func registerCallbackForEvent(event: NotificationEvent, callback: @escaping (_ notification: Notification) -> Void) {
        NotificationCenter.default.addObserver(forName: Notification.Name(rawValue: event.rawValue), object: nil, queue: nil) { [unowned self] notification in
            print("Handling notification for \(notification.name.rawValue)")
            callback(notification)
        }
    }
    
    func buildSecretTranslator(publicKeyData: Data) {
        verifierPublicKey = publicKeyData
        secretTranslator = (cryptoBox.buildSecretsTranslator(verifierPublicKey: publicKeyData))
    }
    
    func lookForDestroyConnection(){
        registerCallbackForEvent(event: NotificationEvent.CONNECTION_STATUS_CHANGE) { notification in
            print("Handling notification for \(notification.name.rawValue)")
            if let notifyObj = notification.userInfo?["connectionStatus"] as? Data {
                let connStatusID = Int(notifyObj[0])
                    if connStatusID == 1 {
                        print("con statusid:", connStatusID)
                        self.destroyConnection()
                    }
                } else {
                    print("weird reason!!")
                }
            }
        }
    
    func destroyConnection(){
        NotificationCenter.default.removeObserver(self)
        print("destroyed")
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

    func sendData(data: String) {
        var dataInBytes = Data(data.utf8)
        var compressedBytes = try! dataInBytes.gzipped()
        var encryptedData = secretTranslator?.encryptToSend(data: compressedBytes)
        if (encryptedData != nil) {
            DispatchQueue.main.async {
                let transferHandler = TransferHandler.shared
                // DOUBT: why is encrypted data written twice ?
                transferHandler.initialize(initdData: encryptedData!)
                let imsgBuilder = imessage(msgType: .INIT_RESPONSE_TRANSFER, data: encryptedData!)
                transferHandler.sendMessage(message: imsgBuilder)
            }
        }
    }
    
    func writeIdentity() {
        print("::: write idendity called ::: ")
        let publicKey = self.cryptoBox.getPublicKey()
        print("verifier pub key:::", self.verifierPublicKey)
        guard let verifierPublicKey = self.verifierPublicKey else {
            print("Write Identity - Found NO KEY")
            return
        }
        var iv = (self.secretTranslator?.initializationVector())!
        central?.write(serviceUuid: Peripheral.SERVICE_UUID, charUUID: NetworkCharNums.identifyRequestCharacteristic, data: iv + publicKey)
        registerCallbackForEvent(event: NotificationEvent.EXCHANGE_RECEIVER_INFO) { notification in
            EventEmitter.sharedInstance.emitNearbyMessage(event: "exchange-receiver-info", data: Self.EXCHANGE_RECEIVER_INFO_DATA)
        }
    }
}
