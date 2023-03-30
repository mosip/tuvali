import Foundation

class ErrorHandler {
    public static var sharedInstance = ErrorHandler()
    private var onError: ((_ message: String) -> Void)?;

    init() {}

    func handleException(type: exceptionType, error: walletExceptionHandler) {
        if type == .walletException {
          WalletExceptionHandler(exceptionErr: error)
        }
       else if type == .verifierException {}
        else {
            handleUnknownException()
        }
      }

    func setOnError(onError: @escaping (_ message: String) -> Void) {
        self.onError = onError
    }
    
    private func handleUnknownException() {
       os_log(.info, "Error in OpenID4vBLE: %{public}@")
    }
}


enum exceptionType {
    case walletException
    case verifierException
}
