import Foundation

@objc(Wallet)
class Wallet: NSObject {
    
    static let shared = Wallet()
    var advIdentifier: String?
    var central: Central = Central()

    private override init() {}
    
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
    
    func startScanning() {
        central.scanForPeripherals()
    }
}
