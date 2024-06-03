import Foundation
import os
class ErrorHandler {
    public static var sharedInstance = ErrorHandler()
    private var walletExceptionHandler: WalletExceptionHandler?
    private var onError: ((_ message: String, _ code: String) -> Void)?

    private init() {}

    func handleException(type: ExceptionType, error: WalletErrorEnum) {
        if type == .walletException {
            walletExceptionHandler?.handle(error: error)
        } else if type == .verifierException {
        } else {
            handleUnknownException(error: error)
        }
    }

    func setOnError(onError: @escaping (_ message: String, _ code: String) -> Void) {
        self.onError = onError
        walletExceptionHandler = WalletExceptionHandler(error: self.onError!)
    }

    private func handleUnknownException(error: WalletErrorEnum) {
        os_log(.error, "Error in OpenID4vBLE: %{public}@", error.description)
        self.onError?(error.description, error.code)
    }
}

enum ExceptionType {
    case walletException
    case verifierException
}
