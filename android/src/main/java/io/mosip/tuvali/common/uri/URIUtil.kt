package io.mosip.tuvali.common.uri

class URIUtils {
  companion object {
    private const val URI_IDENTIFIER = "OPENID4VP"

    fun build(payload: String): String {
      return "$URI_IDENTIFIER://$payload"
    }

    fun extractPayload(uri: String) = uri.split("$URI_IDENTIFIER://")[1]

    fun isValid(uri: String) = uri.contains("$URI_IDENTIFIER://")

  }
}
