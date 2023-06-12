package io.mosip.tuvali.openid4vpble.events


interface EventWithArgs: Event {
  fun getData(): HashMap<String, String>
}
