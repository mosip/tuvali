
import Foundation

protocol PeripheralCommunicatorProtocol: AnyObject {
    func onTransmissionReportRequest(data: Data?)
    func onResponseSizeWriteSuccess()
    func onVerificationStatusChange(data: Data?)
    func onFailedToSendTransferReportRequest()
}

protocol WalletBleCommunicatorProtocol: AnyObject {
    func onIdentifyWriteSuccess()
    func onDisconnectStatusChange(data: Data?)
    func createConnectionHandler()
    func setVeriferKeyOnSameIdentifier(payload: Data, publicData: Data, completion: (() -> Void))
    func onDisconnect()
}
