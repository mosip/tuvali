package com.ble.central.statemessage

import java.util.*

class ScanStartMessage(val serviceUUID: UUID, val scanRespUUID: UUID, val advPayload: String): ICentralMessage(CentralStates.SCAN_START) {}
