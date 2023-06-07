import Foundation

struct VerificationStatusEvent: Event {
    var status: VerificationStatus

    enum VerificationStatus: Int {
        case ACCEPTED = 0
        case REJECTED = 1
    }
}
