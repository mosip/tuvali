package io.mosip.tuvali.common.events

import io.mosip.tuvali.exception.ErrorCode

interface IEventEmitter {
  fun emitEventWithoutArgs(event: EventWithoutArgs)
  fun emitEventWithArgs(event: EventWithArgs)
  fun emitError(message: String, code: ErrorCode)
}
