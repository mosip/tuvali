package io.mosip.tuvali.common.events

import io.mosip.tuvali.common.events.withArgs.ErrorEvent
import io.mosip.tuvali.exception.ErrorCode

class EventProducer {
  private var consumerCallback: ((Event) -> Unit)? = null;

  fun setConsumer(consumer: (Event) -> Unit) {
    consumerCallback = consumer
  }

  fun emitErrorEvent(message: String, code: ErrorCode) {
    emitEvent(ErrorEvent(message, code.value))
  }

  fun emitEvent(event: Event) {
    consumerCallback?.let { it(event) }
  }

  fun removeConsumer() {
    consumerCallback = null
  }
}
