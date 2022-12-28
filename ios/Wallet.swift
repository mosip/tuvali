//
//  Wallet.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 01/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation

@objc(Wallet)
class Wallet: NSObject {
    
    @objc(getModuleName:withRejecter:)
    func getModuleName(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(["iOS Wallet"])
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    func constantsToExport() -> [AnyHashable: Any] {
        return [
            "name": "wallet",
            "platform": "ios"
        ]
    }
}
