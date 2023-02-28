import Foundation
import CoreBluetooth
import os

@available(iOS 13.0, *)
extension Peripheral: CBPeripheralManagerDelegate {
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            os_log(.info, "Peripheral is in ON state")
            self.setupPeripheralsAndStartAdvertising()
        case .poweredOff:
            os_log(.info, "Peripheral is in OFF state")
        default:
            os_log(.info, "Peripheral is in INVALID state")
        }
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didSubscribeTo characteristic: CBCharacteristic) {
        os_log(.info, "Central subscribed to characteristic")
        peripheral.stopAdvertising()
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, central: CBCentral, didUnsubscribeFrom characteristic: CBCharacteristic) {
        os_log(.info, "Central unsubscribed from characteristic")
    }
    
    func peripheralManager(_ peripheral: CBPeripheralManager, didReceiveWrite requests: [CBATTRequest]) {
        os_log(.info, "Received write request for characteristic")
    }
}
