package io.mosip.tuvali.common.uri

const val URI_IDENTIFIER = "OPENID4VP"

class OpenId4vpURI private constructor() {
  private var uri = ""

  constructor(key: String) : this() {
    uri = "$URI_IDENTIFIER://$key"
  }

  companion object {
    fun fromString(uri: String): OpenId4vpURI {
      val openId4vpURI = OpenId4vpURI()
      openId4vpURI.uri = uri
      return openId4vpURI
    }
  }

  fun extractPayload() = uri.split("$URI_IDENTIFIER://")[1]

  fun isValid() = uri.contains("$URI_IDENTIFIER://")

  override fun toString() = uri
}
