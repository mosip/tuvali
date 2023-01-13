import Foundation

@objc(Wallet)
class Wallet: NSObject {
    
    static let shared = Wallet()
    var advIdentifier: String?
    var central: Central?
    var secretTranslator: SecretTranslator?
    
    private override init() {}
    
    func setSecretTranslator(ss: SecretTranslator){
        print("ss called")
        secretTranslator = ss
    }
    
    @objc(getModuleName:withRejecter:)
    func getModuleName(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(["iOS Wallet"])
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    func constantsToExport() -> [AnyHashable: Any] {
        return [
            "name": "wallet",
            "platform": "ios"
        ]
    }
    
    func setAdvIdentifier(advIdentifier: String) {
        self.advIdentifier = advIdentifier
    }
    
//    func startScanning() {
//        central.scanForPeripherals()
//    }
    
    func isSameAdvIdentifier(advertisementPayload: Data) -> Bool {
        // let advertisementPayloadStr = String(decoding: advertisementPayload, as: UTF8.self)
        let advIdentiferData = hexStringToData(string: advIdentifier!)
        print("::: Ad payload::::", advertisementPayload,", Add Identifier ::::", advIdentiferData)
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
}
