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
  }
}
