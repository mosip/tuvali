import Foundation

enum OpenId4vpError: Error {
    case invalidMTUSizeError(mtu: Int)
    case responseTransferFailure
}

extension OpenId4vpError: CustomStringConvertible {
    public var description: String {
        switch self {
        case .invalidMTUSizeError(let mtu):
            return "Negotiated MTU: \(mtu) is too low."
        case .responseTransferFailure:
            return "failed to write response"
        }
    }
    
    public var code: String {
        switch self {
        case .invalidMTUSizeError(let mtu):
            return "TVW_CON_001"
        case .responseTransferFailure:
            return "TVW_REP_001"
        }
    }
}

// Error Code format
// <Component(2)+Role(1)>(3char)_<Stage>(3char)_<Number>(3char) Eg: TVW-CON-001
// Stage --> CON(Connection) | KEX(Key Exchange) | ENC(Encryption) | TRA(Transfer) | REP(Report) | DEC(Decryption)
// ROLE --> TVW(Tuvali+Wallet) | TVV(Tuvali+Verifier)
// UNK --> If role or stage is not known
