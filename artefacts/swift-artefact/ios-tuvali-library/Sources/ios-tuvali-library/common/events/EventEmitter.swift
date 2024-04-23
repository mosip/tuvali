import Foundation

class EventEmitter {
    var listeners: [((Event) -> Void)] = []
    public static let sharedInstance: EventEmitter = EventEmitter()
    
    func emitEvent(_ event: Event) {
        listeners.forEach { listener in
            listener(event)
        }
    }
    
    func emitErrorEvent(message: String, code: String) {
        var eventData: [String: String] = [:]
        eventData["message"] = message
        emitEvent(ErrorEvent(message: message, code: code))
    }
    
    func addListener(listener: @escaping (Event) -> Void) {
        listeners.append(listener)
    }
    
    func removeListeners() {
        listeners.removeAll()
    }
}
