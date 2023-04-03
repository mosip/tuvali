import Foundation

class WalletExceptionHandler {

    var exceptionErr: WalletErrorEnum? = .none
    private var onError: ((_ message: String) -> Void)?;
    var wallet: Wallet?
    
    init(exceptionErr: WalletErrorEnum){
        self.exceptionErr = exceptionErr
        handle(error: exceptionErr)
    }

    func handle(error: WalletErrorEnum) {
    os_log(.info, "Error in OpenID4vBLE: %{public}@", error.description)
        handleError(error.description)
}
    fileprivate func handleError(_ message: String) {
        wallet?.handleDestroyConnection(isSelfDisconnect: false)
        EventEmitter.sharedInstance.emitNearbyErrorEvent(message: message)
    }
}

enum WalletErrorEnum: Error {
    case invalidMTUSizeError(mtu: Int)
    case responseTransferFailure
}

extension WalletErrorEnum: CustomStringConvertible {
    public var description: String {
        switch self {
        case .invalidMTUSizeError(let mtu):
            return "Negotiated MTU: \(mtu) is too low."
        case .responseTransferFailure:
            return "failed to write response"
        }
    }
}
