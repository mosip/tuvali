import Foundation

class ErrorHandler {
    public static var sharedInstance = ErrorHandler()
    private var onError: ((_ message: String) -> Void)?;
    
    init() {}
    
    func handleException(type: exceptionType, error: walletExceptionHandler) {
        if type == .walletException {
          WalletExceptionHandler(exceptionErr: error)
        }
       else if type == .verifierException {
        //  handleVerifierException(e)
        }
        else {
         // handleUnknownException(e)
        }
       // stopBle()
      }
     
    
    func setOnError(onError: @escaping (_ message: String) -> Void) {
        self.onError = onError
    }
   
    func handle(error: walletExceptionHandler) {
        os_log(.info, "Error in OpenID4vBLE: %{public}@", error.description)
        
        if let onError = self.onError {
            onError(error.description)
        } else {
            os_log(.info, "Failed to send error event to openId4vp module. OnError callback not found.")
        }
    }
}


enum exceptionType {
    case walletException
    case verifierException
}
