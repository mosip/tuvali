package com.ble.peripheral.statemessage

import java.util.*

class AdvertisementStartMessage(val serviceUUID: UUID, val scanRespUUID: UUID, val advPayload: String, val  scanRespPayload: String): IPeripheralMessage(PeripheralStates.ADV_START) {}
