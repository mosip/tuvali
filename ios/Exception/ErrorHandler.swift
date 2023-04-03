import Foundation

class ErrorHandler {
    public static var sharedInstance = ErrorHandler()
    private var onError: ((_ message: String) -> Void)?;

   private init() {}

    func handleException(type: exceptionType, error: WalletErrorEnum) {
        if type == .WalletException {
          WalletExceptionHandler(exceptionErr: error)
        }
       else if type == .VerifierException {}
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
    case WalletException
    case VerifierException
}
