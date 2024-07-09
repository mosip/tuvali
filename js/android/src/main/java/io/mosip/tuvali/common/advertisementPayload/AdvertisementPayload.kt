package io.mosip.tuvali.common.advertisementPayload

import org.bouncycastle.util.encoders.Hex

class AdvertisementPayload {
  companion object {
    fun getAdvPayload(identifier: String, key: ByteArray): ByteArray {
      return identifier.toByteArray() + "_".toByteArray() + key.copyOfRange(0, 5)
    }

    fun getAdvPayload(identifier: String, hexKey: String): ByteArray {
      val key = Hex.decode(hexKey)
      return getAdvPayload(identifier, key)
    }

    fun getScanRespPayload(key: ByteArray): ByteArray {
      return key.copyOfRange(5, 32)
    }
  }
}
