import Foundation
import CoreBluetooth
import os

extension Central: CBCentralManagerDelegate {
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            print("Central Manager state is powered ON")
            scanForPeripherals()
        default:
            print("Central Manager is in powered OFF")
        }
    }
    
    func centralManager(_ central: CBCentralManager, didDiscover peripheral: CBPeripheral, advertisementData: [String : Any], rssi RSSI: NSNumber) {
        
        let dataDict = advertisementData["kCBAdvDataServiceData"] as? [CBUUID: Any?]
        let scanResponseData = dataDict?[CBUUID(string: "AB2A")]  as! Data
        let advertisementData = dataDict?[CBUUID(string: "AB29")]  as! Data
        
        let publicKeyData =  advertisementData.subdata(in: advertisementData.count-5..<advertisementData.count) + scanResponseData
        if #available(iOS 13.0, *) {
            let cryptoBox = WalletCryptoBoxBuilder().build()
            let secretsTranslator = (cryptoBox.buildSecretsTranslator(verifierPublicKey: publicKeyData))
            Wallet.shared.setSecretTranslator(ss: secretsTranslator)
           
            if Wallet.shared.isSameAdvIdentifier(advertisementPayload: advertisementData) {
                print("scan stopped")
                central.stopScan()
            }
        } else {
           print ("deployment target is less")
        }
    }

    
    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        os_log("Connected to peripheral: %@", String(describing: peripheral.name))
        central.stopScan()
    }
    
    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        os_log("Peripheral disconnected")
        self.connectedPeripheral = nil
    }
    
    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
    }
}

