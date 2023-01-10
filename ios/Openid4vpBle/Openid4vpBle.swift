import Foundation

@objc(Openid4vpBle)
class Openid4vpBle: RCTEventEmitter {
    
    override init() {
        super.init()
        EventEmitter.sharedInstance.registerEventEmitter(eventEmitter: self)
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
        Wallet.shared.setAdvIdentifier(advIdentifier: firstPartOfPk as! String)
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
    
    @objc
    func destroyConnection() -> Any {
        return "check" as! Any
    }
    
    @objc
    func send(_ message: String, withCallback callback: RCTResponseSenderBlock) {
        let newMessage = String.init(format: "%::%s", message, "iOS")
        callback([newMessage])
    }
    
    @objc(createConnection:withCallback:)
    func createConnection(_ mode: String, withCallback callback: RCTResponseSenderBlock) {
        let message = String.init(format: "MODE->%s", mode)
        
        switch mode {
        case "advertiser":
         print("advert")
        case "discoverer":
            print("disc")
            Wallet.shared.startScanning()
        default:
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
}

enum modeState {
    case advertiser
    case discoverer
}
