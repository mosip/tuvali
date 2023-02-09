package io.mosip.tuvali.transfer

import android.util.Log
import com.github.snksoft.crc.CRC


object CheckValue {
  private const val logTag = "CheckValue"

  //CRC-16/Kermit: https://reveng.sourceforge.io/crc-catalogue/16.htm#crc.cat.crc-16-kermit
  //width=16 poly=0x1021 init=0x0000 refin=true refout=true xorout=0x0000 check=0x2189 residue=0x0000 name="CRC-16/KERMIT"
  //TODO: Need to identify what is check, and residue in the Kermit algorithm
  private val crc16KermitParameters = CRC.Parameters(16, 0x1021, 0x0000, true, true, 0x0000)

  fun get(data: ByteArray): UShort {
    return CRC.calculateCRC(crc16KermitParameters, data).toUShort()
  }

  fun verify(data: ByteArray, receivedCRC: UShort) : Boolean {
    val calculatedCRC = get(data)
    if(calculatedCRC == receivedCRC)
      return true
    Log.e(logTag, "crcReceivedNotEqualToCrcCalculated crcReceived: $receivedCRC, crcCalculated: $calculatedCRC" )
    return false
  }

}
