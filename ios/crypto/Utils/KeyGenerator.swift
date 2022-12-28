//
//  KeyGenerator.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 24/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation
import CryptoKit

@available(iOS 13.0, *)
class KeyGenerator {
    func generateStrongKeyBasedOnHKDF(sharedSecretKey: SharedSecret, keyLength: Int, infoData: String) -> SymmetricKey {
        let salt = "SHA-256".data(using: .utf8)
        let sharedInfo = infoData.data(using: .utf8)
        let strongKey = sharedSecretKey.hkdfDerivedSymmetricKey(using: SHA256.self, salt: salt!, sharedInfo: sharedInfo!, outputByteCount: keyLength)
        
        return strongKey
    }
}
