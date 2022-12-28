package io.mosip.tuvali.transfer

import org.bouncycastle.util.encoders.Hex
import java.security.MessageDigest


class Util {
  companion object {
    fun getSha256(data: ByteArray): String {
      val md = MessageDigest.getInstance("SHA-256")
      md.update(data)
      return Hex.toHexString(md.digest())
    }

    // Big Endian conversion:
    // Convert int to an array with 2 bytes
    fun intToTwoBytesBigEndian(num: Int): ByteArray {
      if (num < 256) {
        val minValue = 0
        return byteArrayOf(minValue.toByte(), num.toByte())
      }
      return byteArrayOf((num/256).toByte(), (num%256).toByte())
    }

    // Big endian conversion:
    // E.g: If there are two bytes in an array. The first byte of array will treated as MSB
    // and second byte is treated as LSB
    fun twoBytesToIntBigEndian(num: ByteArray): Int {
      //TODO: Document endianness here
      val firstByte = num[0]
      val secondByte = num[1]
      return secondByte.toInt() + (256 * firstByte.toInt())
    }
  }
}
