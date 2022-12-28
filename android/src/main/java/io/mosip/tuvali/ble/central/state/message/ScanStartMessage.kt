package io.mosip.tuvali.ble.central.state.message

import java.util.*

class ScanStartMessage(val serviceUUID: UUID, val advPayload: String): IMessage(
  CentralStates.SCAN_START
)
