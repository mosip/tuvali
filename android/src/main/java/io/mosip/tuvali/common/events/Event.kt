package io.mosip.tuvali.common.events

interface Event {
  val type: String
  val args: HashMap<String, String>?
    get() = null
}
