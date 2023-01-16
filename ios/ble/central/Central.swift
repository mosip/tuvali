import Foundation
import CoreBluetooth
import os

class Central: NSObject {
    
    private var centralManager: CBCentralManager!
    var connectedPeripheral: CBPeripheral?
    var  walletVm: WalletViewModel = WalletViewModel()
    var transferCharacteristic: CBCharacteristic?
    var writeCharacteristic: CBCharacteristic?
    var identifyRequestCharacteristic: CBCharacteristic?
    
    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func scanForPeripherals() {
        centralManager.scanForPeripherals(withServices: [Peripheral.SERVICE_UUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        os_log("scanning happening ::::::::")
    }
    
    func connectToPeripheral(peripheral: CBPeripheral) {
        os_log("Coonecting to peripheral")
        centralManager.connect(peripheral)
    }
    
    func writeData(message: String) {
        
    }
    
    @available(iOS 11.0, *)
    func write(serviceUuid: CBUUID, charUUID: CBUUID, data: Data) {
        if let connectedPeripheral = connectedPeripheral {
            if connectedPeripheral.canSendWriteWithoutResponse {
                let mtu = connectedPeripheral.maximumWriteValueLength(for: .withoutResponse)
                connectedPeripheral.writeValue(data, for: writeCharacteristic!, type: .withoutResponse)
            }
        }
    }
}
