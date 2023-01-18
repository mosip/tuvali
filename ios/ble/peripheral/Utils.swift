import Foundation
import CoreBluetooth

struct Utils {
    static func createCBMutableCharacteristics() -> [CBMutableCharacteristic] {
        return characteristicsMap.map {key, chrTuple in
            let keyUUID = CBUUID(string: key)
            return CBMutableCharacteristic(type: keyUUID, properties: chrTuple.properties, value: chrTuple.value, permissions: chrTuple.permissions)
        }
    }
    
    func currentTimeInMilliSeconds()-> Int
        {
            let currentDate = Date()
            let since1970 = currentDate.timeIntervalSince1970
            return Int(since1970 * 1000)
        }
}
