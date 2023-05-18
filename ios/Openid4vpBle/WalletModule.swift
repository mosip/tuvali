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
        print("startConnection->uri::\(uri)")
        if uri.range(of:"OPENID4VP://") == nil {
            //Todo: Throw an error if uri doesnt have openid4vp
            return
        }
        let advPayload = uri.components(separatedBy: "OPENID4VP://")[1]
        wallet = Wallet()
        if advPayload == "" {
            //Todo: Throw an error if advpayload is not there
            return
        }
        wallet?.setAdvIdentifier(identifier: advPayload)
        wallet?.startScanning()
        wallet?.createConnection = {
            EventEmitter.sharedInstance.emitDataEvent(eventType: EventEmitter.EventTypeWithoutData.CONNECTED)
            self.wallet?.writeToIdentifyRequest()
        }
    }

    func stringToJson(jsonText: String) -> NSDictionary {
        var dictonary: NSDictionary?
        if let data = jsonText.data(using: String.Encoding.utf8) {
            do {
                dictonary = try JSONSerialization.jsonObject(with: data, options: []) as? [String:AnyObject] as NSDictionary?
            } catch let error as NSError {
                os_log(.error, " %{public}@ ", error)
            }
        }
        return dictonary!
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

}
