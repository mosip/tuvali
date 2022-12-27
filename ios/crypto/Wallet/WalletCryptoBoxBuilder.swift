//
//  WalletCryptoBoxBuilder.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 24/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation

@available(iOS 13.0, *)
class WalletCryptoBoxBuilder {
    func build() -> WalletCryptoBox {
        return WalletCryptoBoxImpl()
    }
}
