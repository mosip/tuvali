package com.ble.handlercommand

import java.util.*

class AdvertisementCommand(commandType: ICommand.PeripheralStates, val serviceUUID: UUID, val scanRespUUID: UUID, val advPayload: String, val  scanRespPayload: String): ICommand(commandType) {}
