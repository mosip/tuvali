import Foundation
import CoreBluetooth

protocol TransferHandlerDelegate: AnyObject {
    func write(serviceUuid: CBUUID, charUUID: CBUUID, data: Data, withResponse: Bool)
}

extension Wallet: WalletProtocol {
    
    func onDisconnect() {
        self.onDeviceDisconnected()
    }
    
    func onIdentifyWriteSuccess() {
        EventEmitter.sharedInstance.emitDataEvent(event: EventEmitter.EventTypeWithoutData.KEY_EXCHANGE_SUCCESS)
    }
    
    func onDisconnectStatusChange(data: Data?) {
        print("Handling notification for disconnect handle")
        if let data {
            let connStatusID = Int(data[0])
            if connStatusID == 1 {
                print("con statusid:", connStatusID)
                handleDestroyConnection(isSelfDisconnect: false)
            }
        } 
    }
    
    func setVeriferKeyOnSameIdentifier(payload: Data, publicData: Data, completion: (() -> Void)) {
        if isSameAdvIdentifier(advertisementPayload: payload) {
            setVerifierPublicKey(publicKeyData: publicData)
            completion()
        }
    }
    
    func createConnectionHandler() {
        createConnection?()
    }
}

extension Wallet: TransferHandlerDelegate {
    func write(serviceUuid: CBUUID, charUUID: CBUUID, data: Data, withResponse: Bool) {
        if withResponse {
            central?.writeWithResponse(serviceUuid: serviceUuid, charUUID: charUUID, data: data)
        } else {
            central?.writeWithoutResp(serviceUuid: serviceUuid, charUUID: charUUID, data: data)
        }
    }
}


