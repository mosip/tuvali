package io.mosip.tuvali.common.events.withArgs

import io.mosip.tuvali.common.events.Event

data class ErrorEvent(val message: String, val code: String): Event {

}
