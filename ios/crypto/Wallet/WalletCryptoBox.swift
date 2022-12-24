//
//  WalletCryptoBox.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 24/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation
import CryptoKit

protocol WalletCryptoBox {
    func buildSecretsTranslator(verifierPublicKey: Data) -> SecretTranslator
    func getPublicKey() -> Data
}
