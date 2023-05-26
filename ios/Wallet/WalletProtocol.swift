//
//  WalletProtocol.swift
//  react-native-openid4vp-ble
//
//  Created by Tilak Puli on 26/05/23.
//

import Foundation

protocol WalletProtocol: AnyObject {
    func startConnection(_ uri: String)
    func disconnect()
    func send(_ payload: String) 
}
