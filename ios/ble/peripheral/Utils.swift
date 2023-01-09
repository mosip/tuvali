import Foundation
import CoreBluetooth

struct Utils {
    
    static func createCBMutableCharacteristics() -> [CBMutableCharacteristic] {
        return characteristics.map {key, chrTuple in
            let keyUUID = CBUUID(string: key)
            return CBMutableCharacteristic(type: keyUUID, properties: chrTuple.properties, value: chrTuple.value, permissions: chrTuple.permissions)
        }
    }
}
