package io.mosip.tuvali.ble.central.state.message

import java.util.*

class NotificationReceivedMessage(val charUUID: UUID, val value: ByteArray) : IMessage(CentralStates.NOTIFICATION_RECEIVED)
