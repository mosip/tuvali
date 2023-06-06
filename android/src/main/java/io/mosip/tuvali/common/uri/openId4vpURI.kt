package io.mosip.tuvali.common.uri

import android.net.Uri
import android.text.TextUtils.isEmpty

private const val URI_IDENTIFIER = "OPENID4VP"
private const val NAME_QUERY_PARAM_NAME = "name"
private const val KEY_QUERY_PARAM_NAME = "key"

class OpenId4vpURI {
  private var uri: Uri

  constructor(name: String, hexPK: String) {
    this.uri = Uri.Builder()
      .scheme("$URI_IDENTIFIER://connect")
      .appendQueryParameter(NAME_QUERY_PARAM_NAME, name)
      .appendQueryParameter(KEY_QUERY_PARAM_NAME, hexPK).build()
  }

  constructor(uri: String) {
    try {
      this.uri = Uri.parse(uri)
    } catch (e: Exception) {
      this.uri = Uri.EMPTY
    }
  }

  fun getName() = uri.getQueryParameter(NAME_QUERY_PARAM_NAME).orEmpty()

  fun getHexPK() = uri.getQueryParameter(KEY_QUERY_PARAM_NAME).orEmpty()

  fun isValid() = !(isEmpty(getName()) || isEmpty(getHexPK()))

  override fun toString() = uri.toString()
}
