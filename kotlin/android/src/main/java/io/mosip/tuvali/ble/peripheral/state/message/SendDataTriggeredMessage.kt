package io.mosip.tuvali.ble.peripheral.state.message

import java.util.UUID

class SendDataTriggeredMessage(val charUUID: UUID, val isNotificationTriggered: Boolean): IMessage(PeripheralMessageTypes.SEND_DATA_NOTIFIED) {}
