//
//  Wallet+Extension.swift
//  Openid4vpBle
//
//  Created by Dhivya Venkatachalam on 24/02/23.
//

import Foundation
import CoreBluetooth

protocol TransferHandlerDelegate: AnyObject {
    func write(serviceUuid: CBUUID, charUUID: CBUUID, data: Data, withResponse: Bool)
}

extension Wallet: WalletProtocol {
    func onIdentifyWriteSuccess() {
        EventEmitter.sharedInstance.emitNearbyMessage(event: "exchange-receiver-info", data: Self.EXCHANGE_RECEIVER_INFO_DATA)
    }
    
    func onDisconnectStatusChange(data: Data?) {
        print("Handling notification for disconnect handle")
        if let data {
            let connStatusID = Int(data[0])
            if connStatusID == 1 {
                print("con statusid:", connStatusID)
                destroyConnection()
            }
        } else {
            print("weird reason!!")
        }
    }
    
    func hasSameIdentifier(payload: Data, publicData: Data, completion: (() -> Void)) {
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
            central?.write(serviceUuid: serviceUuid, charUUID: charUUID, data: data)
        } else {
            central?.writeWithoutResp(serviceUuid: serviceUuid, charUUID: charUUID, data: data)
        }
    }
}


