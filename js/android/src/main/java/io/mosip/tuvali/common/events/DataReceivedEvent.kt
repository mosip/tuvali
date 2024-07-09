package io.mosip.tuvali.common.events

data class DataReceivedEvent(val data: String, val crcFailureCount: Int, val totalChunkCount: Int): Event
