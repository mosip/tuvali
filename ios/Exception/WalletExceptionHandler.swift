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
//    if let onError = self.onError {
//        onError(error.description)
//        handleError(error.description)
//    } else {
//        os_log(.info, "Failed to send error event to openId4vp module. OnError callback not found.")
//    }
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

protocol exceptionProtocol {
    func setOnError(onError: @escaping (_ message: String) -> Void)
}
