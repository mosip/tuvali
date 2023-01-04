//
//  PeripheralExtension.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 03/01/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import Foundation
import CoreBluetooth
import os

extension Peripheral: CBPeripheralManagerDelegate {
    func peripheralManagerDidUpdateState(_ peripheral: CBPeripheralManager) {
        switch peripheral.state {
        case .poweredOn:
            os_log("Peripheral is in ON state", log: .default, type: .info)
            self.setupPeripheral()
        case .poweredOff:
            os_log("Peripheral is in OFF state", log: .default, type: .error)
        default:
            os_log("Peripheral is in INVALID state", log: .default, type: .error)
        }
    }
}
