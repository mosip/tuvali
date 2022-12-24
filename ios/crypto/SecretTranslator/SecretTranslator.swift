//
//  SecretTranslator.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 24/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation

protocol SecretTranslator {
    func initializationVector() -> Data
    func encryptToSend(data: Data) -> Data
    func decryptUponReceive(data: Data) -> Data
}
