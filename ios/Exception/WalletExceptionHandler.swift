import Foundation

class WalletExceptionHandler {

    private var onError: ((_ message: String) -> Void)?;
    var wallet: Wallet?
    
    init(exceptionErr: WalletErrorEnum){
        handle(error: exceptionErr)
    }

    func handle(error: WalletErrorEnum) {
    os_log(.error, "Error in OpenID4vBLE: %{public}@", error.description)
        handleError(error.description)
}
    fileprivate func handleError(_ message: String) {
        wallet?.handleDestroyConnection(isSelfDisconnect: false)
        EventEmitter.sharedInstance.emitNearbyErrorEvent(message: message)
    }
}

enum WalletErrorEnum: Error {
    case InvalidMTUSizeError(mtu: Int)
    case ResponseTransferFailure
}

extension WalletErrorEnum: CustomStringConvertible {
    public var description: String {
        switch self {
        case .InvalidMTUSizeError(let mtu):
            return "Negotiated MTU: \(mtu) is too low."
        case .ResponseTransferFailure:
            return "failed to write response"
        }
    }
}
