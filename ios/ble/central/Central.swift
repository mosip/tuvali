import Foundation
import CoreBluetooth
import os

class Central: NSObject {
    private var centralManager: CBCentralManager!
    
    var connectedPeripheral: CBPeripheral?
    
    override init() {
        super.init()
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func scanForPeripherals() {
        centralManager.scanForPeripherals(withServices: [Peripheral.SERVICE_UUID, Peripheral.SCAN_RESPONSE_SERVICE_UUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
    }
    
    func connectToPeripheral(peripheral: CBPeripheral) {
        os_log("Coonecting to peripheral")
        centralManager.connect(peripheral)
    }
    
    func writeData(message: String) {
    }
}

