package com.ble.peripheral.state.message

import java.util.*

class AdvertisementStartMessage(val serviceUUID: UUID, val scanRespUUID: UUID, val advPayload: String, val  scanRespPayload: String): IMessage(
  PeripheralMessageTypes.ADV_START
) {}
