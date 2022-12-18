package com.ble.peripheral.state.message

import java.util.UUID

@OptIn(ExperimentalUnsignedTypes::class)
class SendDataMessage(val serviceUUID: UUID, val charUUID: UUID, val data: UByteArray): IMessage(PeripheralMessageTypes.SEND_DATA) {}
