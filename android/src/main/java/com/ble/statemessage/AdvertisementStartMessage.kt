package com.ble.statemessage

import java.util.*

class AdvertisementStartMessage(commandType: IMessage.PeripheralStates, val serviceUUID: UUID, val scanRespUUID: UUID, val advPayload: String, val  scanRespPayload: String): IMessage(commandType) {}
