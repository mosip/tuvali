import Foundation

class WalletExceptionHandler {

    var exceptionErr: walletExceptionHandler? = .none
    private var onError: ((_ message: String) -> Void)?;
    var wallet: Wallet?
    
    init(exceptionErr: walletExceptionHandler){
        self.exceptionErr = exceptionErr
        handle(error: exceptionErr)
    }

    func handle(error: walletExceptionHandler) {
    os_log(.info, "Error in OpenID4vBLE: %{public}@", error.description)
        handleError(error.description)
}
    fileprivate func handleError(_ message: String) {
        wallet?.handleDestroyConnection(isSelfDisconnect: false)
        EventEmitter.sharedInstance.emitNearbyErrorEvent(message: message)
    }
}

enum walletExceptionHandler: Error {
    case invalidMTUSizeError(mtu: Int)
    case responseTransferFailure
}

extension walletExceptionHandler: CustomStringConvertible {
    public var description: String {
        switch self {
        case .invalidMTUSizeError(let mtu):
            return "Negotiated MTU: \(mtu) is too low."
        case .responseTransferFailure:
            return "failed to write response"
        }
    }
}
