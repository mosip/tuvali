//
//  Verifier.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 01/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation

@objc(Verifier)
class Verifier: NSObject {
    
    @objc(getModuleName:withRejecter:)
    func getModuleName(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        resolve(["iOS Verifier"])
    }
    
    @objc
    static func requiresMainQueueSetup() -> Bool {
        return true
    }
    
    @objc
    func constantsToExport() -> [AnyHashable: Any] {
        return [
            "name": "verifier",
            "platform": "ios"
        ]
    }
}

