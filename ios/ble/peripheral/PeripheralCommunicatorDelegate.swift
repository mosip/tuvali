
import Foundation

protocol PeripheralCommunicatorDelegate: AnyObject {
    func transmissionReportHandler(data: Data?)
    func writeSuccessHandler()
    func verificationStatusChange(data: Data?)
}

protocol WalletProtocol: AnyObject {
    func exchangeReceiverInfoHandler()
    func disconnectHandler(data: Data?)
}
