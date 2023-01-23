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

            // TODO - subscribe to the characteristics for (2035, 2036, 2037)
            
            //            if characteristic.uuid == TransferService.characteristicUUID {
            //                self.transferCharacteristic = characteristic
            //                peripheral.setNotifyValue(true, for: characteristic)
            //            }
            //            if characteristic.uuid == TransferService.writeCharacteristic {
            //                print("Found write characteristic")
            //                // kludge: create a static side-effect for default chunk size
            //                BLEConstants.DEFAULT_CHUNK_SIZE = peripheral.maximumWriteValueLength(for: .withResponse)
            //                print("MTU set to be", BLEConstants.DEFAULT_CHUNK_SIZE)
            //                self.writeCharacteristic = characteristic
            //                // No notify required, right?
            //            }
            //            if characteristic.uuid == TransferService.identifyRequestCharacteristic {
            //                self.identifyRequestCharacteristic = characteristic
            ////                sendPublicKey()
            ////                print(characteristic)
        }
        NotificationCenter.default.post(name: Notification.Name(rawValue: "CREATE_CONNECTION"), object: nil)
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        print("Central was able to update value for the characteristic: ", characteristic.uuid.uuidString)
        if let error = error {
            os_log("Unable to recieve updates from device: %s", error.localizedDescription)
            return
        }
        
        // use the new data from subscribed publisher
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            os_log("Unable to write to characteristic: %@", error.localizedDescription)
        }
        print("TS::: Identify:::", characteristic.uuid)
        EventEmitter.sharedInstance.emitNearbyMessage(event: "exchange-receiver-info", data: "{\"deviceName\":\"verifier\"}")
        print("Central was able to write value for the characteristic: ", characteristic.uuid.uuidString)
        if characteristic.uuid.uuidString == "2033" {
            NotificationCenter.default.post(name: Notification.Name(rawValue: "RESPONSE_SIZE_WRITE_SUCCESS"), object: nil)
        } else if characteristic.uuid.uuidString == "2035" {
            let report = characteristic.value
            NotificationCenter.default.post(name: Notification.Name(rawValue: "HANDLE_TRANSMISSION_REPORT"), object: report)
            //
        }
        else {
            
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
        
    }
}
