package io.mosip.tuvali.ble.central.state.message

import java.util.*

class ScanStartMessage(val serviceUUID: UUID): IMessage(
  CentralStates.SCAN_START
)
