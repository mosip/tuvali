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
            let publicKeyData =  advertisementData.subdata(in: advertisementData.count-5..<advertisementData.count) + scanResponseData
            walletBleCommunicatorDelegate?.setVeriferKeyOnSameIdentifier(payload: advertisementData, publicData: publicKeyData) {
                peripheral.delegate = self
                central.connect(peripheral)
                connectedPeripheral = peripheral
            }
        }
    }

    func centralManager(_ central: CBCentralManager, didConnect peripheral: CBPeripheral) {
        os_log(.info, "Connected to peripheral: %{public}s", String(describing: peripheral.name))
        central.stopScan()
        peripheral.discoverServices([Peripheral.SERVICE_UUID])
    }

    func centralManager(_ central: CBCentralManager, didDisconnectPeripheral peripheral: CBPeripheral, error: Error?) {
        os_log(.info, "Peripheral disconnected")
        if let connectedPeripheral = connectedPeripheral {
            central.cancelPeripheralConnection(connectedPeripheral)
        }
        walletBleCommunicatorDelegate?.onDisconnect()
    }

    func centralManager(_ central: CBCentralManager, didFailToConnect peripheral: CBPeripheral, error: Error?) {
    }
}
