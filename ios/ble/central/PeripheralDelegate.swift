import Foundation
import CoreBluetooth
import os
@available(iOS 13.0, *)
extension Central: CBPeripheralDelegate {
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error = error {
            retryServicesDiscovery(peripheral)
            return
        }

        guard let peripheralServices = peripheral.services else {
            retryServicesDiscovery(peripheral)
            return
        }

        let serviceUUIDS = peripheralServices.map({ service in
            return service.uuid
        })

        if !serviceUUIDS.contains(Peripheral.SERVICE_UUID) {
            retryServicesDiscovery(peripheral)
            return
        }

        for service in peripheralServices where Peripheral.SERVICE_UUID == service.uuid {
            peripheral.discoverCharacteristics(CharacteristicIds.allCases.map{CBUUID(string: $0.rawValue)}, for: service)
        }

        print("found \(String(describing: peripheral.services?.count)) services for peripheral \(String(describing: peripheral.name))")
    }

    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let error = error {
            retryCharacteristicsDiscovery(peripheral,service)
            return
        }
        guard let serviceCharacteristics = service.characteristics else {
            retryCharacteristicsDiscovery(peripheral,service)
            return
        }
        for characteristic in serviceCharacteristics {
            // store a reference to the discovered characteristic in the Central for write.
            print("Characteristic UUID:: ", characteristic.uuid.uuidString)
            if characteristic.uuid == NetworkCharNums.SUBMIT_RESPONSE_CHAR_UUID {
                // BLEConstants.DEFAULT_CHUNK_SIZE = peripheral.maximumWriteValueLength(for: .withoutResponse)
            }
            self.cbCharacteristics[characteristic.uuid.uuidString] = characteristic
            // subscribe to the characteristics for (2036, 2037)
            if characteristic.uuid == NetworkCharNums.TRANSFER_REPORT_RESPONSE_CHAR_UUID ||
                characteristic.uuid == NetworkCharNums.VERIFICATION_STATUS_CHAR_UUID || characteristic.uuid == NetworkCharNums.DISCONNECT_CHAR_UUID
            {
                peripheral.setNotifyValue(true, for: characteristic)
            }
        }

        NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.CREATE_CONNECTION.rawValue), object: nil)
    }


    func retryServicesDiscovery(_ peripheral : CBPeripheral){
        if retryStrategy.shouldRetry() {
            let waitTime = retryStrategy.getWaitTime()
            os_log("Error while discovering services retrying again after %d time", waitTime)
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(waitTime))) {
                peripheral.discoverServices([Peripheral.SERVICE_UUID])
            }
        }
        else {
            os_log("Error while discovering services after retrying multiple times")
            retryStrategy.reset()
            return
        }
    }

    func retryCharacteristicsDiscovery(_ peripheral : CBPeripheral, _ service : CBService){
        if retryStrategy.shouldRetry() {
            let waitTime = retryStrategy.getWaitTime()
            os_log("Error while discovering services retrying again after %d time", waitTime)
            DispatchQueue.main.asyncAfter(deadline: .now() + .milliseconds(Int(waitTime))) {
                peripheral.discoverCharacteristics(CharacteristicIds.allCases.map{CBUUID(string: $0.rawValue)}, for: service)
            }
        }
        else {
            os_log("Error while discovering services after retrying multiple times")
            retryStrategy.reset()
            return
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        print("Central was able to update value for the characteristic: ", characteristic.uuid.uuidString)
        if let error = error {
            os_log("Unable to recieve updates from device: %s", error.localizedDescription)
            return
        }
        if characteristic.uuid == NetworkCharNums.TRANSFER_REPORT_RESPONSE_CHAR_UUID {
            let report = characteristic.value as Data?
            print("ts report is :::", report)
            // TODO: figure out why object isn't sent out across
            NotificationCenter.default.post(name: Notification.Name(rawValue: "HANDLE_TRANSMISSION_REPORT"), object: nil, userInfo: ["report": report])
        } else if characteristic.uuid == NetworkCharNums.VERIFICATION_STATUS_CHAR_UUID {
            let verificationStatus = characteristic.value as Data?
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.VERIFICATION_STATUS_RESPONSE.rawValue), object: nil, userInfo: ["status": verificationStatus])
        } else if characteristic.uuid == NetworkCharNums.DISCONNECT_CHAR_UUID {
            let disconnectStatus = characteristic.value as Data?
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.DISCONNECT_STATUS_CHANGE.rawValue), object: nil, userInfo: ["disconnectStatus": disconnectStatus])
            peripheral.setNotifyValue(false, for: characteristic)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            os_log("Unable to write to characteristic: %@", error.localizedDescription)
        }

        if characteristic.uuid == NetworkCharNums.IDENTIFY_REQUEST_CHAR_UUID {
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.EXCHANGE_RECEIVER_INFO.rawValue), object: nil)
        } else if characteristic.uuid == NetworkCharNums.RESPONSE_SIZE_CHAR_UUID {
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.RESPONSE_SIZE_WRITE_SUCCESS.rawValue), object: nil)
        } else if characteristic.uuid == NetworkCharNums.SUBMIT_RESPONSE_CHAR_UUID {
            NotificationCenter.default.post(name: Notification.Name(rawValue: NotificationEvent.INIT_RESPONSE_CHUNK_TRANSFER.rawValue), object: nil)
        }
    }

    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {
    }
}
