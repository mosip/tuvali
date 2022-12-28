//
//  CipherBox.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 22/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation

protocol CipherBox {
    func encrypt(message: Data) -> Data
    func decrypt(message: Data) -> Data
}
