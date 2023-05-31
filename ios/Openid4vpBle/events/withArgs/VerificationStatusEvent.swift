import Foundation

struct VerificationStatusEvent: EventWithArgs {
    var type: String { return "onVerificationStatusReceived" }
    var status: VerificationStatus
    
    func getData() -> [String: String] {
        return ["status": status.rawValue]
    }
    
    enum VerificationStatus: Int {
        case ACCEPTED = 0
        case REJECTED = 1
    }
}

