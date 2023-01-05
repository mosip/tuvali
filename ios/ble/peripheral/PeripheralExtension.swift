import Foundation
import CoreBluetooth
import os

extension Peripheral: CBPeripheralManagerDelegate {
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            os_log("Peripheral is in ON state", log: .default, type: .info)
            self.setupPeripheral()
            self.peripheralManager.startAdvertising([Self.SCAN_RESPONSE_SERVICE_UUID])
        case .poweredOff:
            os_log("Peripheral is in OFF state", log: .default, type: .error)
        default:
            os_log("Peripheral is in INVALID state", log: .default, type: .error)
        }
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didSubscribeTo characteristic: CBCharacteristic) {
        os_log("Central subscribed to characteristic")
        peripheral.stopAdvertising()
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didUnsubscribeFrom characteristic: CBCharacteristic) {
        os_log("Central unsubscribed from characteristic")
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        os_log("Received write request for characteristic")
    }
}
