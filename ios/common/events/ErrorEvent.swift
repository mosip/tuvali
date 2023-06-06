import Foundation

struct ErrorEvent: Event {
    var type: String { return "onError" }
    var message: String
    var code: String
    
    func getData() -> [String: String] {
        return ["message": message, "code": code]
    }
}

