import Foundation

struct VerificationStatusEvent: EventWithArgs {
    var type: String { return "onVerificationStatusReceived" }
    var status: VerificationStatus
    
    func getData() -> [String: String] {
        return ["status": status.rawValue]
    }
    
    enum VerificationStatus: String {
        case ACCEPTED = "ACCEPTED"
        case REJECTED = "REJECTED"
    }
}

