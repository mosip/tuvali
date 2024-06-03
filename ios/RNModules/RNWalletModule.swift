import Foundation

@available(iOS 13.0, *)

class WalletModule: RCTEventEmitter {
    var wallet: WalletProtocol = Wallet()
    var tuvaliVersion: String = "unknown"

    override init() {
        super.init()
        RNEventEmitter.sharedInstance.registerEventEmitter(producer: self)
        wallet.subscribe { event in
            RNEventEmitter.sharedInstance.emitEvent(eventMap: RNEventMapper.toMap(event))
        }
    }

    @objc func noop() -> Void {}

    @objc func startConnection(_ uri: String) {
        wallet.startConnection(uri)
    }

    @objc func disconnect(){
        wallet.disconnect()
    }

    @objc func sendData(_ payload: String) {
        wallet.send(payload)
    }

    @objc override func supportedEvents() -> [String]! {
        return RNEventEmitter.sharedInstance.allEvents
    }

    @objc override static func requiresMainQueueSetup() -> Bool {
        return false
    }
}
