import Foundation

class WalletExceptionHandler {

    private var onError: ((_ message: String, _ code: String) -> Void)?
    
    init(error: (@escaping (String, String) -> Void)) {
        self.onError = error
    }

    func handle(error: WalletErrorEnum) {
        os_log(.error, "Error in OpenID4vBLE: %{public}@", error.description)
        self.onError?(error.description, error.code)
    }
}

enum WalletErrorEnum: Error {
    case invalidURIException
    case invalidMTUSizeError(mtu: Int)
    case responseTransferFailure
}

extension WalletErrorEnum: CustomStringConvertible {
    public var description: String {
        switch self {
        case .invalidURIException:
            return "Received an invalid URI."
        case .invalidMTUSizeError(let mtu):
            return "Negotiated MTU: \(mtu) is too low."
        case .responseTransferFailure:
            return "failed to write response"
        }
    }
    
    public var code: String {
           switch self {
           case .invalidURIException:
               return "TVW_CON_001"
           case .invalidMTUSizeError( _):
               return "TVW_CON_002"
           case .responseTransferFailure:
               return "TVW_REP_001"
           }
       }
}
