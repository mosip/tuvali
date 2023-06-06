import Foundation

struct DataSentEvent: Event {
    var type: String { return "onDataSent" }
    
    func getData() -> [String : String] {
        return [:]
    }
}

