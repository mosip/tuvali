import Foundation
import CrcSwift

class CRC {
    static func evaluate(d: Data) -> UInt16 {
        return CrcSwift.computeCrc16(d, mode: .ccittFalse)
    }

    static func verify(d: Data, expected: UInt16) -> Bool {
        let got = evaluate(d: d)
        if got == expected {
            return true
        }
        os_log("non-equal CRC; evaluated=\(got), expect=\(expected)")
        return false
    }
}
