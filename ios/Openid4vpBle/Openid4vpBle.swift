import Foundation

@objc(Openid4vpBle)
class Openid4vpBle: NSObject {
    
    @objc
    func noop() -> Void {}
    
    @objc
    func getConnectionParameters() -> String {
        return "GetConnectionParameters"
    }
    
    @objc
    func setConnectionParameters(params: String) -> Void {
        print("SetConnectionParameters->Params::\(params)")
    }
    
    @objc
    func getConnectionParametersDebug() -> String {
        return "GetConnectionParametersDebug"
    }
    
    @objc
    func destroyConnection() -> Void {}
    
    @objc(send:withCallback:)
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
    static func requiresMainQueueSetup() -> Bool {
        return false
    }
}
