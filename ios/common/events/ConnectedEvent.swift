import Foundation

struct ConnectedEvent: Event {
    var type: String { return "onConnected" }
    
    func getData() -> [String : String] {
        return [:]
    }
}
