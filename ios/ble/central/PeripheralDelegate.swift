import Foundation
import CoreBluetooth
import os

@available(iOS 13.0, *)
extension Central: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error = error {
            os_log("Error while discovering services: %s", error.localizedDescription)
            return
        }
        
        guard let peripheralServices = peripheral.services else { return }
        for service in peripheralServices where Peripheral.SERVICE_UUID == service.uuid {
            peripheral.discoverCharacteristics(CharacteristicIds.allCases.map{CBUUID(string: $0.rawValue)}, for: service)
        }
        print("found \(String(describing: peripheral.services?.count)) services for peripheral \(String(describing: peripheral.name))")
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let error = error {
            os_log("Error discovering Characteristics: %s", error.localizedDescription)
            return
        }
        guard let serviceCharacteristics = service.characteristics else { return }
        for characteristic in serviceCharacteristics {
            // store a reference to the discovered characteristic in the Central for write.
            print("Characteristic UUID:: ", characteristic.uuid.uuidString)
            self.cbCharacteristics[characteristic.uuid.uuidString] = characteristic
            // subscribe to the characteristics for (2035, 2036, 2037)
            if characteristic.uuid == NetworkCharNums.semaphoreCharacteristic ||
                characteristic.uuid == NetworkCharNums.verificationStatusCharacteristic
            {
                peripheral.setNotifyValue(true, for: characteristic)
            }
        }
        
        NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.CREATE_CONNECTION.rawValue), object: nil)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        print("Central was able to update value for the characteristic: ", characteristic.uuid.uuidString)
        if let error = error {
            os_log("Unable to recieve updates from device: %s", error.localizedDescription)
            return
        }
        if characteristic.uuid.uuidString == "2035" {
            let report = characteristic.value
            print("ts report is :::", report)
            NotificationCenter.default.post(name: Notification.Name(rawValue: "HANDLE_TRANSMISSION_REPORT"), object: report)
         
        }
        
        if characteristic.uuid == NetworkCharNums.semaphoreCharacteristic {
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.HANDLE_TRANSMISSION_REPORT.rawValue), object: nil)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            os_log("Unable to write to characteristic: %@", error.localizedDescription)
        }
        
        if characteristic.uuid == NetworkCharNums.identifyRequestCharacteristic {
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.EXCHANGE_RECEIVER_INFO.rawValue), object: nil)
        }
        if characteristic.uuid == NetworkCharNums.responseSizeCharacteristic {
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.RESPONSE_SIZE_WRITE_SUCCESS.rawValue), object: nil)
        } else if characteristic.uuid == NetworkCharNums.responseCharacteristic {
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.INIT_RESPONSE_CHUNK_TRANSFER.rawValue), object: nil)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
    }
}
