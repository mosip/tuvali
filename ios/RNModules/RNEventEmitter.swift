import Foundation

@available(iOS 13.0, *)
class RNEventEmitter: RNEventEmitterProtocol {

    public static var sharedInstance = RNEventEmitter()
    private var EVENT_NAME = "DATA_EVENT"
    static var producer: WalletModule!

    private  init() {}

    func registerEventEmitter(producer: WalletModule) {
        RNEventEmitter.producer = producer
    }

    func dispatch(name: String, body: Any?) {
        RNEventEmitter.producer.sendEvent(withName: name, body: body)
    }
    
    func emitEvent(event: Event) {
        let writableMap = NSMutableDictionary()
        writableMap["type"] = event.type
        writableMap.addEntries(from: event.getData())
        dispatch(name: EVENT_NAME, body: writableMap)
    }

    lazy var allEvents: [String] = {
        return [
            EVENT_NAME
        ]
    }()
}


