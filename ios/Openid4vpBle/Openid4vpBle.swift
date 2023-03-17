import Foundation

@available(iOS 13.0, *)
@objc(Openid4vpBle)
class Openid4vpBle: RCTEventEmitter {
    var wallet: Wallet?

    var tuvaliVersion: String = "unknown"
    override init() {
        super.init()
        EventEmitter.sharedInstance.registerEventEmitter(eventEmitter: self)
        ErrorHandler.sharedInstance.setOnError(onError: self.handleError)
    }

    @objc
    func noop() -> Void {}

    @objc
    func getConnectionParameters() -> String {
        return "GetConnectionParameters"
    }

    @objc(setConnectionParameters:)
    func setConnectionParameters(params: String) -> Any {
        print("SetConnectionParameters->Params::\(params)")
        let connectionParameter = stringToJson(jsonText: params)
        let publicKey = connectionParameter["pk"] as? String
        print("synchronized setConnectionParameters called with", params, "and", publicKey)
        wallet = Wallet()
        if let publicKey {
            wallet?.setAdvIdentifier(identifier: publicKey)
        }
        return "data" as Any
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

    @objc
    func getConnectionParametersDebug() -> String {
        return "GetConnectionParametersDebug"
    }

    @objc(setTuvaliVersion:)
    func setTuvaliVersion(version: String) -> String{
        tuvaliVersion = version
        os_log("Tuvali version - %{public}@",tuvaliVersion);
        return tuvaliVersion
      }

    @objc(destroyConnection:)
    func destroyConnection(withCallback callback: @escaping RCTResponseSenderBlock) -> Any {
        wallet?.handleDestroyConnection(isSelfDisconnect: true)
        wallet = nil
        return "check" as! Any
    }

    @objc
    func send(_ message: String, withCallback callback: @escaping RCTResponseSenderBlock) {
        let messageComponents = message.components(separatedBy: "\n")

        switch messageComponents[0] {
        case "exchange-receiver-info":
            os_log(.info, "EXCHANGE-RECEIVER-INFO")
            callback([])
        case "exchange-sender-info":
            os_log(.info, "EXCHANGE-SENDER-INFO")
            callback([])
            wallet?.writeToIdentifyRequest()
        case "send-vc":
            callback([])
            wallet?.sendData(data: messageComponents[1])
            os_log(.info, ">> raw message size : %{public}d", messageComponents[1].count)
        default:
            os_log(.info, "DEFAULT SEND: MESSAGE : %{public}s ", message)
        }
    }

    @objc(createConnection:withCallback:)
    func createConnection(_ mode: String, withCallback callback: @escaping RCTResponseSenderBlock) {
        switch mode {
        case "advertiser":
            os_log(.info, "Advertiser")
        case "discoverer":
            os_log(.info, "Discoverer")
            wallet?.startScanning()
            wallet?.createConnection = {
                callback([])
            }
        default:
            os_log(.info, "DEFAULT CASE: MESSAGE :  %{public}s", mode)
            break
        }
    }

    @objc
     override func supportedEvents() -> [String]! {
        return EventEmitter.sharedInstance.allEvents
    }

    @objc
    override static func requiresMainQueueSetup() -> Bool {
        return false
    }

    fileprivate func handleError(_ message: String) {
        wallet?.handleDestroyConnection(isSelfDisconnect: false)
        EventEmitter.sharedInstance.emitNearbyErrorEvent(message: message)
    }

}
