import Foundation

@available(iOS 13.0, *)
@objc(Openid4vpBle)
class Openid4vpBle: RCTEventEmitter {
    
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
        var paramsObj = stringToJson(jsonText: params)
        var firstPartOfPk = paramsObj["pk"]
        print("synchronized setConnectionParameters called with", params, "and", firstPartOfPk)
        Wallet.shared.setAdvIdentifier(identifier: firstPartOfPk as! String)
        return "data" as Any
    }

    func stringToJson(jsonText: String) -> NSDictionary {
        var dictonary: NSDictionary?
        if let data = jsonText.data(using: String.Encoding.utf8) {
            do {
                dictonary = try JSONSerialization.jsonObject(with: data, options: []) as? [String:AnyObject] as NSDictionary?
            } catch let error as NSError {
                print(error)
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
//        tuvaliVersion = version
//        os_log("Tuvali version - %{public}@",tuvaliVersion);
//        Central.shared.tuvaliVersion = tuvaliVersion
        return tuvaliVersion
      }

    @objc(destroyConnection:)
    func destroyConnection(withCallback callback: @escaping RCTResponseSenderBlock) -> Any {
        Wallet.shared.destroyConnection()
        return "check" as! Any
    }

    @objc
    func send(_ message: String, withCallback callback: @escaping RCTResponseSenderBlock) {
        let messageComponents = message.components(separatedBy: "\n")
        //print("new message is :::: ", messageComponents)

        switch messageComponents[0] {
        case "exchange-receiver-info":
            //print("EXCHANGE-RECEIVER-INFO")
            callback([])
        case "exchange-sender-info":
            //print("EXCHANGE-SENDER-INFO")
            callback([])
            Wallet.shared.writeToIdentifyRequest()
        case "send-vc":
            callback([])
            os_log(.info, ">> raw message size : %{public}d", messageComponents[1].count)
            Wallet.shared.sendData(data: messageComponents[1])
        default:
            print("DEFAULT SEND: MESSAGE:: ", message)
        }
    }

    @objc(createConnection:withCallback:)
    func createConnection(_ mode: String, withCallback callback: @escaping RCTResponseSenderBlock) {
        switch mode {
        case "advertiser":
            os_log("Advertiser")
        case "discoverer":
            os_log("Discoverer")
            Central.shared.initialize()
            Wallet.shared.central = Central.shared
            Central.shared.createConnection = {
                callback([])
            }
        default:
            print("DEFAULT CASE: MESSAGE:: ", mode)
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
        Wallet.shared.destroyConnection()
        EventEmitter.sharedInstance.emitNearbyErrorEvent(message: message)
    }

}
