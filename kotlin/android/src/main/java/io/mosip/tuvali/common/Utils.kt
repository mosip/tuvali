package io.mosip.tuvali.common

import java.nio.ByteBuffer

class Utils {
  companion object {
    fun longToBytes(value: Long): ByteArray {
      val buffer: ByteBuffer = ByteBuffer.allocate(Long.SIZE_BYTES)
      buffer.putLong(value)
      return buffer.array()
    }
  }
}
