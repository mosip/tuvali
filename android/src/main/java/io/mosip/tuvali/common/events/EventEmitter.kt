package io.mosip.tuvali.common.events

import io.mosip.tuvali.exception.ErrorCode

class EventEmitter {
  private var consumers = mutableListOf<((Event) -> Unit)>()

  fun addConsumer(consumer: (Event) -> Unit) {
    consumers.add(consumer)
  }

  fun emitErrorEvent(message: String, code: ErrorCode) {
    emitEvent(ErrorEvent(message, code.value))
  }

  fun emitEvent(event: Event) {
    consumers.forEach{it(event)}
  }

  fun removeConsumers() {
    consumers.clear()
  }
}
