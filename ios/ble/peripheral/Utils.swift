import Foundation
import CoreBluetooth
import CryptoKit

@available(iOS 13.0, *)
struct Utils {
    static func createCBMutableCharacteristics() -> [CBMutableCharacteristic] {
        return characteristicsMap.map {key, chrTuple in
            let keyUUID = CBUUID(string: key)
            return CBMutableCharacteristic(type: keyUUID, properties: chrTuple.properties, value: chrTuple.value, permissions: chrTuple.permissions)
        }
    }
    
    static func currentTimeInMilliSeconds()-> UInt64 {
        let currentDate = Date()
        let since1970 = currentDate.timeIntervalSince1970
        return UInt64(since1970 * 1000)
    }
    
    static func symKeyToString(key: SymmetricKey) -> String {
        let hexKey = key.withUnsafeBytes {
            return Data(Array($0)).toHex()
        }
        return hexKey
    }
    
    static func twoBytesToIntBigEndian(num: Data) -> Int {
        return (Int(num[0]) * 256 + Int(num[1]) )
    }
}
