import Foundation
import CoreBluetooth

struct BLEConstants {
    static let SERVICE_UUID = CBUUID(string: "00000001-0000-1000-8000-00805f9b34fb")
    static let SCAN_RESPONSE_SERVICE_UUID = CBUUID(string: "00000002-0000-1000-8000-00805f9b34fb")
    static var ADV_IDENTIFIER = ""
    static var verifierPublicKey: Data = Data()
    static var DEFAULT_CHUNK_SIZE = 512
    static var seqNumberReservedByteSize = 2
    static var mtuReservedByteSize = 2
    static let EXCHANGE_RECEIVER_INFO_DATA = "{\"deviceName\":\"Verifier\"}"
    // Set maximum attribute value as defined by spec Core 5.3
    // ref: https://github.com/dariuszseweryn/RxAndroidBle/pull/808
    // TODO: decide if this should be bumped up to 512
    static let MAX_ALLOWED_DATA_LEN = 509
}
