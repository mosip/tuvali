
import Foundation

protocol PeripheralCommunicatorDelegate: AnyObject {
    func transmissionReportHandler(data: Data?)
    func writeSuccessHandler()
    func verificationStatusChange(data: Data?)
    func exchangeReceiverInfoHandler()
}

protocol WalletProtocol: AnyObject {
    func exchangeReceiverInfoHandler()
}
