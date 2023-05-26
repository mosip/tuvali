import Foundation

@available(iOS 13.0, *)
class EventEmitter {

    public static var sharedInstance = EventEmitter()
    private var EVENT_NAME = "DATA_EVENT"
    static var producer: WalletModule!

    private  init() {}

    func registerEventEmitter(producer: WalletModule) {
        EventEmitter.producer = producer
    }

    func dispatch(name: String, body: Any?) {
        EventEmitter.producer.sendEvent(withName: name, body: body)
    }

    func emitEventWithoutArgs(event: EventWithoutArgs) {
        let writableMap = NSMutableDictionary()
        writableMap["type"] = event.type
        dispatch(name: EVENT_NAME, body: writableMap)
    }

    func emitEventWithArgs(event: EventWithArgs) {
        let writableMap = NSMutableDictionary()
        writableMap["type"] = event.type
        writableMap.addEntries(from: event.getData())
        dispatch(name: EVENT_NAME, body: writableMap)
    }

    func emitErrorEvent(message: String, code: String) {
        var eventData: [String: String] = [:]
        eventData["message"] = message
        emitEventWithArgs(event: ErrorEvent(message: message, code: code))
    }

    lazy var allEvents: [String] = {
        return [
            EVENT_NAME
        ]
    }()
}


