import Foundation

@available(iOS 13.0, *)
@objc(WalletModule)
class WalletModule: RCTEventEmitter {
    var wallet: Wallet?
    var tuvaliVersion: String = "unknown"

    override init() {
        super.init()
        EventEmitter.sharedInstance.registerEventEmitter(eventEmitter: self)
        ErrorHandler.sharedInstance.setOnError(onError: self.handleError)
    }


    @objc func noop() -> Void {}

    @objc func startConnection(_ uri: String) {
        let openId4VpURI = OpenId4vpURI(uri: uri)
        print("startConnection->uri::\(uri)")

        guard openId4VpURI.isValid(), let advPayload = getAdvPayload(openId4VpURI) else {
            ErrorHandler.sharedInstance.handleException(type: .walletException, error: .invalidURIException)
            return
        }

        wallet = Wallet()
        wallet?.setAdvIdentifier(identifier: advPayload)
        wallet?.startScanning()
        wallet?.createConnection = {
            EventEmitter.sharedInstance.emitEventWithoutArgs(event: ConnectedEvent())
            self.wallet?.writeToIdentifyRequest()
        }
    }


    @objc func setTuvaliVersion(_ version: String) -> String{
        tuvaliVersion = version
        os_log("Tuvali version - %{public}@",tuvaliVersion);
        return tuvaliVersion
    }

    @objc func disconnect(){
        wallet?.handleDestroyConnection(isSelfDisconnect: true)
        wallet = nil
    }

    @objc func sendData(_ data: String) {
        wallet?.sendData(data: data)
        os_log(.info, ">> raw message size : %{public}d", data.count)
    }

    @objc override func supportedEvents() -> [String]! {
        return EventEmitter.sharedInstance.allEvents
    }

    @objc override static func requiresMainQueueSetup() -> Bool {
        return false
    }

    fileprivate func handleError(_ message: String, _ code: String) {
        wallet?.handleDestroyConnection(isSelfDisconnect: false)
        EventEmitter.sharedInstance.emitErrorEvent(message: message, code: code)
    }

    fileprivate func getAdvPayload(_ openId4VpURI: OpenId4vpURI) -> Data? {
        guard let name = openId4VpURI.getName(), let data = (name + "_").data(using: .utf8), let hexPublickey = openId4VpURI.getHexPK() else {
            return nil
        }
        return data + hexStringToData(string: String(hexPublickey.prefix(10)))
    }
}
