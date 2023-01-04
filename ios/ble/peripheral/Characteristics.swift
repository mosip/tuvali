//
//  Services.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 03/01/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import Foundation
import CoreBluetooth

typealias CharacteristicTuple = (properties: CBCharacteristicProperties, permissions: CBAttributePermissions)

let characteristics: [String: CharacteristicTuple] = [
    "00002030-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write,]), permissions: CBAttributePermissions([.writeable])),
    "00002031-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable])),
    "00002032-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable])),
    "00002033-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable])),
    "00002034-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.writeable])),
    "00002035-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.writeWithoutResponse, .write]), permissions: CBAttributePermissions([.readable, .writeable])),
    "00002036-0000-1000-8000-00805f9b34fb": (properties: CBCharacteristicProperties([.read, .indicate]), permissions: CBAttributePermissions([.readable])),
]

struct Characteristic {
    let name: String
    let type: String
    let value: Data?
    let properties: CBCharacteristicProperties
    let permissions: CBAttributePermissions
    
    func getCBUUIDType() -> CBUUID {
        return CBUUID(string: self.type)
    }
    
    func getCBMutableCharateristic() -> CBMutableCharacteristic {
        return CBMutableCharacteristic(type: getCBUUIDType(), properties: self.properties, value: value, permissions: self.permissions)
    }
}


