import Foundation
import Gzip

@objc(Wallet)
@available(iOS 13.0, *)
class WalletBleCommunicator: NSObject {

    var central: Central?
    var secretTranslator: SecretTranslator?
    var cryptoBox: WalletCryptoBox = WalletCryptoBoxBuilder().build()
    var advIdentifier: Data?
    var verifierPublicKey: Data?
    var createConnection: (() -> Void)?
    static let EXCHANGE_RECEIVER_INFO_DATA = "{\"deviceName\":\"Verifier\"}"

    override init() {
        super.init()
        central = Central()
    }

    @objc(getModuleName:withRejecter:)
    func getModuleName(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(["iOS Wallet"])
    }

    func setAdvIdentifier(identifier: Data) {
        self.advIdentifier = identifier
    }

    func setVerifierPublicKey(publicKeyData: Data) {
        verifierPublicKey = publicKeyData
    }

    func startScanning(){
        central?.walletBleCommunicatorDelegate = self
    }

    func handleDestroyConnection(isSelfDisconnect: Bool) {
        central?.disconnect()
        if !isSelfDisconnect {
            onDeviceDisconnected()
        }
    }

    func onDeviceDisconnected(){
        EventEmitter.sharedInstance.emitEvent(DisconnectedEvent())
    }

    func isSameAdvIdentifier(advertisementPayload: Data) -> Bool {
        guard let advIdentifier = advIdentifier else {
            os_log(.info, "Found NO ADV Identifier")
            return false
        }
        let advIdentifierData = advIdentifier
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

    func send(_ payload: String) {
        var dataInBytes = Data(payload.utf8)
        var compressedBytes = try! dataInBytes.gzipped()
        var encryptedData = secretTranslator?.encryptToSend(data: compressedBytes)

        if (encryptedData != nil) {
            DispatchQueue.main.async {
                let transferHandler = TransferHandler()
                transferHandler.delegate = self
                transferHandler.destroyConnection = { [weak self] in
                    self?.handleDestroyConnection(isSelfDisconnect: true)
                }
                // DOUBT: why is encrypted data written twice ?

                self.central?.delegate = transferHandler
                transferHandler.initialize(initdData: encryptedData!)
                var currentMTUSize = self.central?.connectedPeripheral?.maximumWriteValueLength(for: .withoutResponse)
                if currentMTUSize == nil || currentMTUSize! < 0 {
                   currentMTUSize = BLEConstants.DEFAULT_CHUNK_SIZE
                }
                // follow BLE 5.3 spec where max data size in a chunk is limited to 512
                // now iOS impl is compatible to Android13 which follows BLE5.3 spec more closely
                currentMTUSize =  min(currentMTUSize!, BLEConstants.MAX_ALLOWED_DATA_LEN)
                let imsgBuilder = imessage(msgType: .INIT_RESPONSE_TRANSFER, data: encryptedData!, mtuSize: currentMTUSize)
                transferHandler.sendMessage(message: imsgBuilder)
            }
        }
    }

    func writeToIdentifyRequest() {
        let publicKey = self.cryptoBox.getPublicKey()
        guard let verifierPublicKey = self.verifierPublicKey else {
            os_log(.info, "Write Identify - Found NO KEY")
            return
        }
        secretTranslator = (cryptoBox.buildSecretsTranslator(verifierPublicKey: verifierPublicKey))
        var nonce = (self.secretTranslator?.getNonce())!
        central?.writeWithResponse(serviceUuid: Peripheral.SERVICE_UUID, charUUID: NetworkCharNums.IDENTIFY_REQUEST_CHAR_UUID, data: nonce + publicKey)
    }
}
