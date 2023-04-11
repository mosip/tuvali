import Foundation
import CoreBluetooth
import CryptoKit

@available(iOS 13.0, *)
struct Util {
    enum ByteCount: Int{
        case FourBytes = 4
        case TwoBytes = 2
    }
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

    static func networkOrderedByteArrayToInt(num: Data) -> Int {
        let value = UInt16(bigEndian: num.withUnsafeBytes { $0.pointee })
        return Int(value)
    }

  static func intToNetworkOrderedByteArray(num: Int, byteCount: ByteCount) -> Data {
      switch byteCount{
      case .FourBytes :
          return withUnsafeBytes(of: UInt32(num).bigEndian) { Data($0) }

      case .TwoBytes :
          return withUnsafeBytes(of: UInt16(num).bigEndian) { Data($0) }

      }

    }

}
