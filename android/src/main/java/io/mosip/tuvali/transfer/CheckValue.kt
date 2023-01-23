package io.mosip.tuvali.transfer

import android.util.Log
import com.github.snksoft.crc.CRC


object CheckValue {
  private const val logTag = "CheckValue"

  fun get(data: ByteArray): UShort {
    return CRC.calculateCRC(CRC.Parameters.CCITT, data).toUShort()
  }

  fun verify(data: ByteArray, receivedCRC: UShort) : Boolean {
    val calculatedCRC = get(data)
    if(calculatedCRC == receivedCRC)
      return true
    Log.e(logTag, "crcReceivedNotEqualToCrcCalculated crcReceived: $receivedCRC, crcCalculated: $calculatedCRC" )
    return false
  }

}
