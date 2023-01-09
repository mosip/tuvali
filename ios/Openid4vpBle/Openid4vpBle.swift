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
    
    @objc
    func setConnectionParameters(params: String) {
        print("SetConnectionParameters->Params::\(params)")
    }
    
    @objc
    func getConnectionParametersDebug() -> String {
        return "GetConnectionParametersDebug"
    }
    
    @objc
    func destroyConnection() {}
    
    @objc
    func send(_ message: String, withCallback callback: RCTResponseSenderBlock) {
        let newMessage = String.init(format: "%::%s", message, "iOS")
        callback([newMessage])
    }
    
    @objc(createConnection:withCallback:)
    func createConnection(_ mode: String, withCallback callback: RCTResponseSenderBlock) {
        let message = String.init(format: "MODE->%s", mode)
        callback([message])
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
