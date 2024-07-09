package io.mosip.tuvali.ble.peripheral.state.message

import java.util.*

class AdvertisementStartMessage(val serviceUUID: UUID, val scanRespUUID: UUID, val advPayload: ByteArray, val  scanRespPayload: ByteArray): IMessage(
  PeripheralMessageTypes.ADV_START
) {}
