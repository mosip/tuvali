import Foundation
import CrcSwift

//CRC-16/Kermit: https://reveng.sourceforge.io/crc-catalogue/16.htm#crc.cat.crc-16-kermit
//width=16 poly=0x1021 init=0x0000 refin=true refout=true xorout=0x0000 check=0x2189 residue=0x0000 name="CRC-16/KERMIT"
//TODO: Need to identify what is check, and residue in the Kermit algorithm

class CRC {
    static func evaluate(d: Data) -> UInt16 {
        let crc = CrcSwift.computeCrc16(
            d,
            initialCrc: 0x0000,
            polynom: 0x1021,
            xor: 0x0000,
            refIn: true,
            refOut: true
        )
        return crc
    }

    static func verify(d: Data, expected: UInt16) -> Bool {
        let got = evaluate(d: d)
        if got == expected {
            return true
        }
        os_log(.error, "non-equal CRC; evaluated= %{public}@, expect= %{public}@", got, expected)
        return false
    }
}
