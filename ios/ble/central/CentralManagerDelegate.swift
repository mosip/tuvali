import Foundation
import CoreBluetooth
import os

@available(iOS 13.0, *)
extension Central {
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        
        let dataDict = advertisementData["kCBAdvDataServiceData"] as? [CBUUID: Any?]
        if let uuidDict = dataDict, let data = uuidDict[CBUUID(string: "AB2A")], let data = data {
            let scanResponseData = dataDict?[CBUUID(string: "AB2A")]  as! Data
            let advertisementData = dataDict?[CBUUID(string: "AB29")]  as! Data
            print("adv data::", advertisementData, "scan resuly:::", scanResponseData)
            let publicKeyData =  advertisementData.subdata(in: advertisementData.count-5..<advertisementData.count) + scanResponseData
            print("veri pub key::", publicKeyData)
            if Wallet.shared.isSameAdvIdentifier(advertisementPayload: advertisementData) {
                Wallet.shared.setVerifierPublicKey(publicKeyData: publicKeyData)
                peripheral.delegate = self
                central.connect(peripheral)
                connectedPeripheral = peripheral
            }
        }
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        os_log("Connected to peripheral: %@", String(describing: peripheral.name))
        central.stopScan()
        peripheral.discoverServices([Peripheral.SERVICE_UUID])
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        os_log("Peripheral disconnected")
        if let connectedPeripheral = connectedPeripheral {
            central.cancelPeripheralConnection(connectedPeripheral)
        }
        Wallet.shared.onDeviceDisconnected(isManualDisconnect: false)
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
    }
}
