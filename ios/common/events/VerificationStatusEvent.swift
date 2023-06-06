import Foundation

struct VerificationStatusEvent: Event {
    var type: String { return "onVerificationStatusReceived" }
    var status: VerificationStatus

    func getData() -> [String: String] {
        return ["status": String(status.rawValue)]
    }

    enum VerificationStatus: Int {
        case ACCEPTED = 0
        case REJECTED = 1
    }
}

