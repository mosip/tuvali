package io.mosip.tuvali.ble.peripheral.state.message

import java.util.UUID

class SendDataMessage(val serviceUUID: UUID, val charUUID: UUID, val data: ByteArray): IMessage(PeripheralMessageTypes.SEND_DATA) {}
