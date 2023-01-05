//
//  Utils.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 05/01/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import Foundation
import CoreBluetooth

struct Utils {
    
    static func createCBMutableCharacteristics() -> [CBMutableCharacteristic] {
        return characteristics.map {key, chrTuple in
            return CBMutableCharacteristic(type: key, value: chrTuple.value, properties: chrTuple.properties, permissions: chrTuple.permissions)
        }
    }
}
