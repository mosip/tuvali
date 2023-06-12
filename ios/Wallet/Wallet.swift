import Foundation

class Wallet: WalletProtocol {
    
    var bleCommunicator: WalletBleCommunicator?
    
    init() {
       ErrorHandler.sharedInstance.setOnError(onError: self.handleError)
   }

    func startConnection(_ uri: String) {
        print("startConnection->uri::\(uri)")
        let openId4VpURI = OpenId4vpURI(uri: uri)
            
        guard openId4VpURI.isValid(), let advPayload = getAdvPayload(openId4VpURI) else {
            ErrorHandler.sharedInstance.handleException(type: .walletException, error: .invalidURIException)
            return
        }
        
        bleCommunicator = WalletBleCommunicator()
        bleCommunicator?.setAdvIdentifier(identifier: advPayload)
        bleCommunicator?.startScanning()
        bleCommunicator?.createConnection = {
            EventEmitter.sharedInstance.emitEvent(ConnectedEvent())
            self.bleCommunicator?.writeToIdentifyRequest()
        }
    }

    func disconnect(){
        bleCommunicator?.handleDestroyConnection(isSelfDisconnect: true)
        bleCommunicator = nil
    }

    func send(_ payload: String) {
        bleCommunicator?.send(payload)
        os_log(.info, ">> raw message size : %{public}d", payload.count)
    }

    func subscribe(_ listener: @escaping (Event) -> Void) {
        EventEmitter.sharedInstance.addListener(listener: listener)
    }
    
    func unsubscribe() {
        EventEmitter.sharedInstance.removeListeners()
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
