package com.ble.central.state.message

import java.util.*

class ScanStartMessage(val serviceUUID: UUID, val scanRespUUID: UUID, val advPayload: String): IMessage(
  CentralStates.SCAN_START
)
