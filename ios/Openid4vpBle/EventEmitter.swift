import Foundation

@available(iOS 13.0, *)
class EventEmitter {

    public static var sharedInstance = EventEmitter()
    private var EVENT_NAME = "DATA_EVENT"
    static var eventEmitter: WalletModule!

    private  init() {}

    func registerEventEmitter(eventEmitter: WalletModule) {
        EventEmitter.eventEmitter = eventEmitter
    }

    func dispatch(name: String, body: Any?) {
        EventEmitter.eventEmitter.sendEvent(withName: name, body: body)
    }

    func emitDataEvent(eventType: EventTypeWithoutData) {
        let writableMap = NSMutableDictionary()
        writableMap["type"] = eventType.rawValue
        dispatch(name: EVENT_NAME, body: writableMap)
    }

    func emitTransferUpdateEvent(status: TransferUpdateStatus) {
        let writableMap = NSMutableDictionary()
        writableMap["type"] = EventTypeWithData.TRANSFER_STATUS_UPDATE.rawValue
        writableMap["status"] = status.rawValue
        dispatch(name: EVENT_NAME, body: writableMap)
    }

    func emitErrorEvent(message: String, code: String) {
        var eventData: [String: String] = [:]
        eventData["message"] = message
        eventData["code"] = String(code)
        eventData["type"] = EventTypeWithData.ERROR.rawValue
        dispatch(name: EVENT_NAME, body: eventData)
    }

    func emitVCReceivedEvent(vc: String) {
        let writableMap = NSMutableDictionary()
        writableMap["type"] = EventTypeWithData.VC_RECEIVED.rawValue
        writableMap["vc"] = vc
        dispatch(name: EVENT_NAME, body: writableMap)
    }

    func emitVerificationStatusEvent(status: VerificationStatus) {
        let writableMap = NSMutableDictionary()
        writableMap["type"] = EventTypeWithData.VERIFICATION_STATUS.rawValue
        writableMap["status"] = status.rawValue
        dispatch(name: EVENT_NAME, body: writableMap)
    }

    enum TransferUpdateStatus: String {
        case SUCCESS = "SUCCESS"
        case FAILURE = "FAILURE"
        case IN_PROGRESS = "IN_PROGRESS"
        case CANCELLED = "CANCELLED"
    }

    enum EventTypeWithoutData: String {
        case CONNECTED = "onConnected"
        case KEY_EXCHANGE_SUCCESS = "onKeyExchangeSuccess"
        case DISCONNECTED = "onDisconnected"
    }

    enum EventTypeWithData: String {
        case TRANSFER_STATUS_UPDATE = "onTransferStatusUpdate"
        case VC_RECEIVED = "onVCReceived"
        case ERROR = "onError"
        case VERIFICATION_STATUS = "onVerificationStatusReceived"
    }

    enum VerificationStatus: String {
        case ACCEPTED = "ACCEPTED"
        case REJECTED = "REJECTED"
    }

    lazy var allEvents: [String] = {
        return [
            EVENT_NAME
        ]
    }()
}


