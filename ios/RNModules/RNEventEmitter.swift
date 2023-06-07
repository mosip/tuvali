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
        writableMap["type"] = getEventType(event)
        let eventMirror = Mirror(reflecting: event)
    
        for child in eventMirror.children {
            writableMap[child.label ?? "invalidLabel"] = child.value
        }

        dispatch(name: EVENT_NAME, body: writableMap)
    }
    
    func getEventType(_ event: Event) -> String {
        switch event {
        case is ConnectedEvent: return "onConnected"
        case is SecureChannelEstablishedEvent: return "onSecureChannelEstablished"
        case is DataSentEvent: return "onDataSent"
        case is VerificationStatusEvent: return "onVerificationStatusReceived"
        case is ErrorEvent: return  "onError"
        case is DisconnectedEvent: return "onDisconnected"
        default:
            os_log(.error, "Invalid event type")
            return ""
        }
    }
    
    lazy var allEvents: [String] = {
        return [
            EVENT_NAME
        ]
    }()
}


