import Foundation

struct SecureChannelEstablishedEvent: Event {
    var type: String { return "onSecureChannelEstablished" }
    
    func getData() -> [String : String] {
        return [:]
    }
}

