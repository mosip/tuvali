//
//  CipherPackage.swift
//  Openid4vpBle
//
//  Created by Shaik mohammed Jaffer on 22/12/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

import Foundation

class CipherPackage {
    private var myself: CipherBox
    private var other: CipherBox
    
    init(myself: CipherBox, other: CipherBox) {
        self.myself = myself
        self.other = other
    }
    
    var getSelfCipherBox: CipherBox {
        return self.myself
    }
    
    var getOtherCipherBox: CipherBox {
        return self.other
    }
}

