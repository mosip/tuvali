//
//  CentralExtension.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 03/01/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import Foundation
import CoreBluetooth

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
        os_log("Appending peripheral - %@ to list", String(describing: peripheral.name))
        print("CBAdvertisementDataServiceUUIDsKey adv value " + String(describing: advertisementData[CBAdvertisementDataServiceUUIDsKey]))
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
        os_log("Failed to connect to peripheral: \(String(describing: error.debugDescription))")
        connectedToPeripheral = false
    }
}

func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
    if let error = error {
        os_log("Error while discovering services: %s", error.localizedDescription)
        cleanup()
        return
    }
    
    os_log("Discovering services for \(String(describing: peripheral.name))")
    
    guard let peripheralServices = peripheral.services else { return }
    for service in peripheralServices {
        peripheral.discoverCharacteristics([TransferService.characteristicUUID], for: service)
    }
}

func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
    if let error = error {
        os_log("Error discovering Characteristics: %s", error.localizedDescription)
        return
    }
    
    os_log("Discovering characteristics for \(String(describing: peripheral.name))")

    guard let serviceCharacteristics = service.characteristics else { return }
    for characteristic in serviceCharacteristics where characteristic.uuid == TransferService.characteristicUUID {
        self.transferCharacteristic = characteristic
        peripheral.setNotifyValue(true, for: characteristic)
    }
}

extension Central: CBPeripheralDelegate {
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverServices error: Error?) {
        if let error = error {
            os_log("Error while discovering services: %s", error.localizedDescription)
            cleanup()
            return
        }
        
        os_log("Discovering services for \(String(describing: peripheral.name))")
        
        guard let peripheralServices = peripheral.services else { return }
        for service in peripheralServices {
            peripheral.discoverCharacteristics([TransferService.characteristicUUID], for: service)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didDiscoverCharacteristicsFor service: CBService, error: Error?) {
        if let error = error {
            os_log("Error discovering Characteristics: %s", error.localizedDescription)
            return
        }
        
        os_log("Discovering characteristics for \(String(describing: peripheral.name))")

        guard let serviceCharacteristics = service.characteristics else { return }
        for characteristic in serviceCharacteristics where characteristic.uuid == TransferService.characteristicUUID {
            self.transferCharacteristic = characteristic
            peripheral.setNotifyValue(true, for: characteristic)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didUpdateValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            os_log("Unable to recieve updates from device: %s", error.localizedDescription)
            cleanup()
            return
        }
        
        // use the new data from subscribed publisher
    }
    
    func peripheral(_ peripheral: CBPeripheral, didWriteValueFor characteristic: CBCharacteristic, error: Error?) {
        if let error = error {
            os_log("Unable to write to characteristic: %@", error.localizedDescription)
        }
    }
    
    func peripheral(_ peripheral: CBPeripheral, didModifyServices invalidatedServices: [CBService]) {}
}
