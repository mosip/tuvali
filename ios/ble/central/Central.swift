import Foundation
import CoreBluetooth
import os

@available(iOS 13.0, *)
class Central: NSObject, CBCentralManagerDelegate {
    
    private var centralManager: CBCentralManager!
    var connectedPeripheral: CBPeripheral?
    var transferCharacteristic: CBCharacteristic?
    var writeCharacteristic: CBCharacteristic?
    var identifyRequestCharacteristic: CBCharacteristic?
    
    var chars: [String: CBCharacteristic] = [:]
    
    public static var shared = Central()
    
    func initialize() {
        centralManager = CBCentralManager(delegate: self, queue: nil)
    }
    
    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        switch central.state {
        case .poweredOn:
            print("Central Manager state is powered ON")
            scanForPeripherals()
        default:
            print("Central Manager is in powered OFF")
        }
    }
    
    deinit {
        print("Central is DeInitializing")
    }
    
    func scanForPeripherals() {
        centralManager.scanForPeripherals(withServices: [Peripheral.SERVICE_UUID], options: [CBCentralManagerScanOptionAllowDuplicatesKey: false])
        os_log("scanning happening ::::::::")
    }
    
    func write(serviceUuid: CBUUID, charUUID: CBUUID, data: Data) {
        if chars.contains(where: { key, value in
            if key == charUUID.uuidString { return true }
            return false
        }) {
            print("Value in chars... stopping write")
            return
        }
        
        if let connectedPeripheral = connectedPeripheral {
            if connectedPeripheral.canSendWriteWithoutResponse {
                guard let writeCharacteristic = self.identifyRequestCharacteristic else {
                    print("Write characteristic is NIL")
                    return
                }
                let mtu = connectedPeripheral.maximumWriteValueLength(for: .withResponse)
                print("Write MTU: ", mtu)
                let bytesToCopy: size_t = min(mtu, data.count)
                let messageData = Data(bytes: Array(data), count: bytesToCopy)
                connectedPeripheral.writeValue(messageData, for: writeCharacteristic, type: .withResponse)
            }
        }
    }
    
    func stopWritingToCharacteristic(characteristic: CBCharacteristic) {
        self.chars[characteristic.uuid.uuidString] = characteristic
    }
}
