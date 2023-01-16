
import Foundation
import CoreBluetooth

struct BLEConstants {
    static let SERVICE_UUID = CBUUID(string: "0000AB29-0000-1000-8000-00805f9b34fb")
    static let SCAN_RESPONSE_SERVICE_UUID = CBUUID(string: "0000AB2A-0000-1000-8000-00805f9b34fb")
    static var ADV_IDENTIFIER = ""
    static var verifierPublicKey: Data = Data()
}
