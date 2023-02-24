
import Foundation

protocol PeripheralCommunicatorProtocol: AnyObject {
    func onTransmissionReportRequest(data: Data?)
    func onResponseSizeWriteSuccess()
    func onVerificationStatusChange(data: Data?)
}

protocol WalletProtocol: AnyObject {
    func onIdentifyWriteSuccess()
    func onDisconnectStatusChange(data: Data?)
}
