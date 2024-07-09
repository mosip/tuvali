package io.mosip.tuvali.common.events

data class ErrorEvent(val message: String, val code: String): Event
