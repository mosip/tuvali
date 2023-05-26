package io.mosip.tuvali.common.events


interface EventWithArgs: Event {
  fun getData(): HashMap<String, String>
}
