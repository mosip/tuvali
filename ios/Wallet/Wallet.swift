import Foundation

class Wallet: WalletProtocol {
    var bleCommunicator: WalletBleCommunicator?
    
    init() {
       ErrorHandler.sharedInstance.setOnError(onError: self.handleError)
   }

    func startConnection(_ uri: String) {
        print("startConnection->uri::\(uri)")
            
        guard openId4VpURI.isValid(), let advPayload = getAdvPayload(openId4VpURI) else {
            ErrorHandler.sharedInstance.handleException(type: .walletException, error: .invalidURIException)
            return
        }
        
        bleCommunicator = WalletBleCommunicator()
        bleCommunicator?.setAdvIdentifier(identifier: advPayload)
        bleCommunicator?.startScanning()
        bleCommunicator?.createConnection = {
            EventEmitter.sharedInstance.emitEventWithoutArgs(event: ConnectedEvent())
            self.bleCommunicator?.writeToIdentifyRequest()
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

    func disconnect(){
        bleCommunicator?.handleDestroyConnection(isSelfDisconnect: true)
        bleCommunicator = nil
    }

    func send(_ payload: String) {
        bleCommunicator?.send(payload)
        os_log(.info, ">> raw message size : %{public}d", payload.count)
    }

    fileprivate func handleError(_ message: String, _ code: String) {
        bleCommunicator?.handleDestroyConnection(isSelfDisconnect: false)
        EventEmitter.sharedInstance.emitErrorEvent(message: message, code: code)
    }
    
    fileprivate func getAdvPayload(_ openId4VpURI: OpenId4vpURI) -> Data? {
        guard let name = openId4VpURI.getName(), let data = (name + "_").data(using: .utf8), let hexPublickey = openId4VpURI.getHexPK() else {
            return nil
        }
        return data + hexStringToData(string: String(hexPublickey.prefix(10)))
    }
}
