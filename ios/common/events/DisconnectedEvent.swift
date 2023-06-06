import Foundation

struct DisconnectedEvent: Event {
    var type: String { return "onDisconnected" }
    
    func getData() -> [String : String] {
        return [:]
    }
}

