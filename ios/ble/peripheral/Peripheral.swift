//
//  Peripheral.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 03/01/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import Foundation
import CoreBluetooth

class Peripheral: NSObject {
    private var peripheralManager: CBPeripheralManager!
    
    override init() {
        super.init()
        peripheralManager = CBPeripheralManager(delegate: self, queue: nil, options: [CBPeripheralManagerOptionShowPowerAlertKey: true])
    }
    
    internal func setupPeripheral() {
    }
}
