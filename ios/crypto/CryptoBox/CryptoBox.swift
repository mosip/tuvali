//
//  CryptoBox.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 22/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation
import CryptoKit

protocol CryptoBox {
    func createCipherPackage(otherPublicKey: Data, senderInfo: String, recieverInfo: String, ivBytes: Data) -> CipherPackage
    func getPublicKey() -> Data
}

