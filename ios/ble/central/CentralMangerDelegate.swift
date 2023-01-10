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
        os_log("Discovered peripheral")
       // print("CBAdvertisementDataServiceUUIDsKey adv value " + String(describing: advertisementData[CBAdvertisementDataServiceUUIDsKey]))
        print("expected Data:::::", advertisementData["kCBAdvDataServiceData"] as? [CBUUID: Any?])
        
        let dataDict = advertisementData["kCBAdvDataServiceData"] as? [CBUUID: Any?]
        if let uuidDict = dataDict, let data = uuidDict[CBUUID(string: "AB2A")], let data = data {
           // os_log("Appending peripheral - %@ to list", String(describing: peripheral.name))
            os_log("Scan Response data - %s", String(data: data as! Data, encoding: .utf8) ?? "No Scan Response Data")
            peripherals.append(peripheral)

            let scanResponseData = dataDict?[CBUUID(string: "AB2A")]  as! Data
            let advertisementData = dataDict?[CBUUID(string: "AB29")]  as! Data
//            os_log("advertisement data -> \(advertisementData)")
//            os_log("scan response data -> \(scanResponseData)")
            let publicKeyData =  advertisementData + scanResponseData
//            cryptoBox = WalletCryptoBoxBuilder().build()
//            secretsTranslator = (cryptoBox?.buildSecretsTranslator(verifierPublicKey: publicKeyData))!
        }
        os_log("%@", advertisementData)
        os_log("---------------------------")
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

