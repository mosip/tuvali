package io.mosip.tuvali.transfer

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.bouncycastle.util.encoders.Hex
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import io.mosip.tuvali.openid4vpble.Openid4vpBleModule
import java.nio.ByteBuffer


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
      val byteBuffer = ByteBuffer.allocate(2)
      val byteArray = byteBuffer.putShort(num.toShort()).array()
      byteBuffer.clear()
      return byteArray
    }

    // Big endian conversion: If there are two bytes in an array. The first byte of array will treated as MSB
    // and second byte is treated as LSB
    /*
           +------------+-------------+
           |    (MSB)   |    (LSB)    |
           |  firstByte | secondByte  |
           |   (byte 0) |  (byte 1)   |
           +------------+-------------+
    secondByte.toInt() + (256 * firstByte.toInt())
   */

    @OptIn(ExperimentalUnsignedTypes::class)
    fun twoBytesToIntBigEndian(num: ByteArray): Int {
      val byteBuffer = ByteBuffer.wrap(num)
      val intValue = byteBuffer.short.toInt()
      byteBuffer.clear()
      return intValue
    }

    fun compress(bytes: ByteArray): ByteArray? {
//      return bytes
      val out = ByteArrayOutputStream()
      try {
        GZIPOutputStream(out).use { gzipOutputStream ->
          gzipOutputStream.write(bytes, 0, bytes.size)
          gzipOutputStream.finish()
          return out.toByteArray()
        }
      } catch (e: Exception) {
        throw RuntimeException("Error while compression!", e)
      }
    }

    fun decompress(bytes: ByteArray): ByteArray? {
//      return bytes
      try {
        GZIPInputStream(ByteArrayInputStream(bytes)).use { gzipInputStream -> return gzipInputStream.readBytes() }
      } catch (e: Exception) {
        throw RuntimeException("Error while decompression!", e)
      }
    }

    fun sleepInRealTime(delayTime: Long) {
      runBlocking {
        delay(delayTime)
      }
    }

    fun getLogTag(moduleName: String): String{
      return "${moduleName} : v${Openid4vpBleModule.tuvaliVersion}"
    }
  }
}
